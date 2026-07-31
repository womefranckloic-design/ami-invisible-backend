package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.request.EnvoyerMessageRequest;
import com.franck.amiinvisible.dto.response.*;
import com.franck.amiinvisible.entity.*;
import com.franck.amiinvisible.entity.enums.ModeConfidentialite;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.ConversationLectureRepository;
import com.franck.amiinvisible.repository.ConversationRepository;
import com.franck.amiinvisible.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationLectureRepository conversationLectureRepository;
    private final ActiviteService activiteService;
    private final ParticipantService participantService;

    // F-19, F-20 : les 2 fils du participant (celui ou il offre / celui ou on lui offre)
    // Point 4 : inclut le compteur de messages non lus par conversation
    public List<ConversationResponse> listerMesConversations(Long activiteId, Long participantId) {
        return conversationRepository.findAllForParticipant(activiteId, participantId).stream()
                .map(c -> versConversationResponse(c, participantId))
                .toList();
    }

    // F-22, Point 5 : supervision admin, adaptee au mode de confidentialite de l'activite
    public List<ConversationAdminResponse> listerPourAdmin(Long adminId, Long activiteId) {
        Activite activite = activiteService.getActiviteAppartenantA(adminId, activiteId);
        verifierSupervisionAutorisee(activite);

        return conversationRepository.findByActiviteId(activiteId).stream()
                .map(c -> {
                    Tirage t = c.getTirage();
                    long nbMessages = messageRepository.findByConversationIdOrderByDateEnvoiAsc(c.getId()).size();
                    return new ConversationAdminResponse(
                            c.getId(), t.getOffrant().getNomReel(), t.getOffrant().getIdentifiantAnonyme(),
                            t.getDestinataire().getNomReel(), t.getDestinataire().getIdentifiantAnonyme(), nbMessages
                    );
                })
                .toList();
    }

    // F-21, S-02 : messages d'une conversation, cote participant (jamais de nom reel)
    // Point 4 : consulter une conversation marque ses messages comme lus
    @Transactional
    public List<MessageResponse> consulterMessages(Long activiteId, Long participantId, Long conversationId) {
        Conversation conversation = getConversationDuParticipant(activiteId, participantId, conversationId);

        List<MessageResponse> messages = messageRepository.findByConversationIdOrderByDateEnvoiAsc(conversation.getId()).stream()
                .map(m -> new MessageResponse(
                        m.getId(), m.getExpediteur().getId(),
                        m.getExpediteur().getId().equals(participantId),
                        m.getContenu(), m.getDateEnvoi()
                ))
                .toList();

        marquerCommeLue(conversation, participantId);
        return messages;
    }

    // F-22, Point 5 : messages d'une conversation, vue admin - respecte le mode de confidentialite
    public List<MessageAdminResponse> consulterMessagesAdmin(Long adminId, Long activiteId, Long conversationId) {
        Activite activite = activiteService.getActiviteAppartenantA(adminId, activiteId);
        verifierSupervisionAutorisee(activite);

        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> c.getActivite().getId().equals(activiteId))
                .orElseThrow(() -> ApiException.notFound("Conversation introuvable"));

        boolean contenuVisible = activite.getModeConfidentialite() == ModeConfidentialite.SUPERVISION_TOTALE;

        return messageRepository.findByConversationIdOrderByDateEnvoiAsc(conversation.getId()).stream()
                .map(m -> new MessageAdminResponse(
                        m.getId(), m.getExpediteur().getNomReel(), m.getExpediteur().getIdentifiantAnonyme(),
                        contenuVisible ? m.getContenu() : "[contenu masque - mode supervision technique]",
                        m.getDateEnvoi()
                ))
                .toList();
    }

    @Transactional
    public MessageResponse envoyerMessage(Long activiteId, Long participantId, Long conversationId, EnvoyerMessageRequest request) {
        Conversation conversation = getConversationDuParticipant(activiteId, participantId, conversationId);
        Activite activite = conversation.getActivite();

        if (activite.getStatut() == StatutActivite.CLOTUREE) {
            throw ApiException.badRequest("Cette activite est cloturee, les echanges sont figes");
        }
        if (activite.isLectureSeule()) {
            throw ApiException.badRequest("Cette conversation est en lecture seule");
        }

        Participant expediteur = participantService.getParticipantDansActivite(participantId, activiteId);

        Message message = Message.builder()
                .conversation(conversation)
                .expediteur(expediteur)
                .contenu(request.contenu())
                .dateEnvoi(Instant.now())
                .build();
        message = messageRepository.save(message);

        // L'expediteur a par definition deja "lu" jusqu'a son propre message
        marquerCommeLue(conversation, participantId);

        return new MessageResponse(message.getId(), participantId, true, message.getContenu(), message.getDateEnvoi());
    }

    private void verifierSupervisionAutorisee(Activite activite) {
        if (activite.getModeConfidentialite() == ModeConfidentialite.AUCUNE_SUPERVISION) {
            throw ApiException.forbidden("Cette activite a ete configuree en mode 'aucune supervision' : "
                    + "l'administrateur ne peut pas consulter les conversations");
        }
    }

    // Non bloquant par nature : une collision de concurrence ici (deux requetes
    // qui marquent la meme conversation comme lue au meme instant) ne doit jamais
    // faire echouer la lecture des messages elle-meme.
    private void marquerCommeLue(Conversation conversation, Long participantId) {
        try {
            ConversationLecture lecture = conversationLectureRepository
                    .findByConversationIdAndParticipantId(conversation.getId(), participantId)
                    .orElse(ConversationLecture.builder()
                            .conversation(conversation)
                            .participant(participantService.getParticipantDansActivite(participantId, conversation.getActivite().getId()))
                            .build());
            lecture.setDateDerniereLecture(Instant.now());
            conversationLectureRepository.save(lecture);
        } catch (DataIntegrityViolationException e) {
            // Deux requetes concurrentes ont tente de creer la meme marque de lecture
            // (contrainte unique conversation_id/participant_id) : sans consequence,
            // l'autre requete a deja enregistre la lecture au meme instant.
            log.debug("Course ignoree sur ConversationLecture (conversation={}, participant={})",
                    conversation.getId(), participantId);
        } catch (Exception e) {
            log.warn("Echec non bloquant du marquage de lecture (conversation={}, participant={}) : {}",
                    conversation.getId(), participantId, e.getMessage());
        }
    }

    private long compterNonLus(Conversation conversation, Long participantId) {
        return conversationLectureRepository.findByConversationIdAndParticipantId(conversation.getId(), participantId)
                .map(lecture -> messageRepository.countByConversationIdAndExpediteurIdNotAndDateEnvoiAfter(
                        conversation.getId(), participantId, lecture.getDateDerniereLecture()))
                .orElseGet(() -> messageRepository.countByConversationIdAndExpediteurIdNot(conversation.getId(), participantId));
    }

    private Conversation getConversationDuParticipant(Long activiteId, Long participantId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .filter(c -> c.getActivite().getId().equals(activiteId))
                .orElseThrow(() -> ApiException.notFound("Conversation introuvable"));

        Tirage t = conversation.getTirage();
        boolean appartient = t.getOffrant().getId().equals(participantId) || t.getDestinataire().getId().equals(participantId);
        if (!appartient) {
            // F-20 : un participant ne peut consulter aucune autre conversation que les siennes
            throw ApiException.forbidden("Vous n'avez pas acces a cette conversation");
        }
        return conversation;
    }

    private ConversationResponse versConversationResponse(Conversation c, Long participantId) {
        Tirage t = c.getTirage();
        boolean estOffrant = t.getOffrant().getId().equals(participantId);
        Participant interlocuteur = estOffrant ? t.getDestinataire() : t.getOffrant();
        String role = estOffrant ? "OFFRANT" : "DESTINATAIRE";
        long nbNonLus = compterNonLus(c, participantId);
        return new ConversationResponse(c.getId(), role, participantService.toPublicResponse(interlocuteur), nbNonLus);
    }
}
