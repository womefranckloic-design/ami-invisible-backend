package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.response.MonTirageResponse;
import com.franck.amiinvisible.dto.response.TirageAdminResponse;
import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.Conversation;
import com.franck.amiinvisible.entity.Participant;
import com.franck.amiinvisible.entity.Tirage;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TirageService {

    private final ActiviteService activiteService;
    private final ActiviteRepository activiteRepository;
    private final ParticipantRepository participantRepository;
    private final TirageRepository tirageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ParticipantService participantService;

    // F-14, F-15, F-16, F-17, F-18
    @Transactional
    public List<TirageAdminResponse> lancerTirage(Long adminId, Long activiteId) {
        Activite activite = activiteService.getActiviteAppartenantA(adminId, activiteId);

        if (activite.getStatut() != StatutActivite.INSCRIPTION) {
            throw ApiException.badRequest("Le tirage ne peut etre lance que pendant la periode d'inscription (statut INSCRIPTION). Utilisez la reinitialisation si necessaire.");
        }
        if (activite.isTirageEffectue()) {
            throw ApiException.badRequest("Le tirage a deja ete effectue pour cette activite. Reinitialisez-le d'abord si vous voulez le refaire.");
        }

        List<Participant> participants = new ArrayList<>(participantRepository.findByActiviteIdOrderByDateInscriptionAsc(activiteId));
        if (participants.size() < 2) {
            throw ApiException.badRequest("Il faut au moins 2 participants pour effectuer un tirage");
        }

        List<Participant> ordreTirage = genererDerangement(participants);

        Instant maintenant = Instant.now();
        List<TirageAdminResponse> resultat = new ArrayList<>();

        for (int i = 0; i < ordreTirage.size(); i++) {
            Participant offrant = ordreTirage.get(i);
            Participant destinataire = ordreTirage.get((i + 1) % ordreTirage.size());

            Tirage tirage = Tirage.builder()
                    .activite(activite)
                    .offrant(offrant)
                    .destinataire(destinataire)
                    .dateTirage(maintenant)
                    .build();
            tirage = tirageRepository.save(tirage);

            conversationRepository.save(Conversation.builder()
                    .activite(activite)
                    .tirage(tirage)
                    .build());

            resultat.add(new TirageAdminResponse(
                    tirage.getId(), offrant.getNomReel(), offrant.getIdentifiantAnonyme(),
                    destinataire.getNomReel(), destinataire.getIdentifiantAnonyme()
            ));
        }

        activite.setTirageEffectue(true);
        activite.setStatut(StatutActivite.EN_COURS);
        activite.setDateDebutActivite(maintenant);
        activite.setDateFinActivite(maintenant.plus(activite.getDureeGlobaleJours(), ChronoUnit.DAYS));
        activiteRepository.save(activite);

        return resultat;
    }

    /**
     * F-18 : reinitialisation explicite par l'administrateur. Supprime messages,
     * conversations et tirage existants, puis remet l'activite en periode d'inscription
     * pour permettre un nouveau tirage. Action destructive et irreversible.
     */
    @Transactional
    public void reinitialiserTirage(Long adminId, Long activiteId) {
        Activite activite = activiteService.getActiviteAppartenantA(adminId, activiteId);

        if (!activite.isTirageEffectue()) {
            throw ApiException.badRequest("Aucun tirage n'a encore ete effectue pour cette activite");
        }
        if (activite.getStatut() == StatutActivite.CLOTUREE) {
            throw ApiException.badRequest("Impossible de reinitialiser une activite cloturee");
        }

        messageRepository.findByConversationActiviteIdOrderByDateEnvoiAsc(activiteId)
                .forEach(messageRepository::delete);
        conversationRepository.findByActiviteId(activiteId)
                .forEach(conversationRepository::delete);
        tirageRepository.deleteByActiviteId(activiteId);

        activite.setTirageEffectue(false);
        activite.setStatut(StatutActivite.INSCRIPTION);
        activite.setDateDebutActivite(null);
        activite.setDateFinActivite(null);
        activiteRepository.save(activite);
    }

    public List<TirageAdminResponse> consulterTirageAdmin(Long adminId, Long activiteId) {
        activiteService.getActiviteAppartenantA(adminId, activiteId);
        return tirageRepository.findByActiviteId(activiteId).stream()
                .map(t -> new TirageAdminResponse(
                        t.getId(), t.getOffrant().getNomReel(), t.getOffrant().getIdentifiantAnonyme(),
                        t.getDestinataire().getNomReel(), t.getDestinataire().getIdentifiantAnonyme()))
                .toList();
    }

    // F-17 : vue restreinte cote participant
    public MonTirageResponse consulterMonTirage(Long activiteId, Long participantId) {
        Tirage jOffre = tirageRepository.findByActiviteIdAndOffrantId(activiteId, participantId)
                .orElseThrow(() -> ApiException.notFound("Le tirage n'a pas encore ete effectue"));
        Tirage onMOffre = tirageRepository.findByActiviteIdAndDestinataireId(activiteId, participantId)
                .orElseThrow(() -> ApiException.notFound("Le tirage n'a pas encore ete effectue"));

        return new MonTirageResponse(
                participantService.toPublicResponse(jOffre.getDestinataire()),
                participantService.toPublicResponse(onMOffre.getOffrant())
        );
    }

    /**
     * Genere un ordre de participants tel que chaque participant[i] offre a participant[i+1 mod n].
     * Cela garantit : pas d'auto-attribution, pas d'orphelin, chacun offre et recoit exactement une fois (F-15).
     */
    private List<Participant> genererDerangement(List<Participant> participants) {
        List<Participant> copie = new ArrayList<>(participants);
        Collections.shuffle(copie);
        return copie;
    }
}
