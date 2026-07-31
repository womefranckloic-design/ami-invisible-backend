package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.request.CreerActiviteRequest;
import com.franck.amiinvisible.dto.request.ProlongerInscriptionRequest;
import com.franck.amiinvisible.dto.response.ActiviteResponse;
import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.ActiviteRepository;
import com.franck.amiinvisible.repository.ConversationRepository;
import com.franck.amiinvisible.repository.ParticipantRepository;
import com.franck.amiinvisible.repository.TirageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiviteService {

    private final ActiviteRepository activiteRepository;
    private final ParticipantRepository participantRepository;
    private final TirageRepository tirageRepository;
    private final ConversationRepository conversationRepository;
    private final CodeGeneratorService codeGeneratorService;

    // F-01, F-06, F-07, F-08
    @Transactional
    public ActiviteResponse creerActivite(Long adminId, CreerActiviteRequest request) {
        String codeAcces;
        do {
            codeAcces = codeGeneratorService.genererCodeAcces();
        } while (activiteRepository.existsByCodeAcces(codeAcces));

        Instant maintenant = Instant.now();

        Activite activite = Activite.builder()
                .admin(com.franck.amiinvisible.entity.Admin.builder().id(adminId).build())
                .nom(request.nom())
                .description(request.description())
                .nbParticipantsAttendu(request.nbParticipantsAttendu())
                .codeAcces(codeAcces)
                .statut(StatutActivite.INSCRIPTION)
                .dateCreation(maintenant)
                .dateDebutInscription(maintenant)
                .dateFinInscription(maintenant.plus(request.dureeInscriptionHeures(), ChronoUnit.HOURS))
                .dureeGlobaleJours(request.dureeGlobaleJours())
                .modeConfidentialite(request.modeConfidentialite() != null
                        ? request.modeConfidentialite()
                        : com.franck.amiinvisible.entity.enums.ModeConfidentialite.SUPERVISION_TOTALE)
                .build();

        activite = activiteRepository.save(activite);
        return toResponse(activite);
    }

    public List<ActiviteResponse> listerMesActivites(Long adminId) {
        return activiteRepository.findByAdminIdOrderByDateCreationDesc(adminId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ActiviteResponse consulterActivite(Long adminId, Long activiteId) {
        return toResponse(getActiviteAppartenantA(adminId, activiteId));
    }

    // F-13 : prolonger le delai d'inscription
    @Transactional
    public ActiviteResponse prolongerInscription(Long adminId, Long activiteId, ProlongerInscriptionRequest request) {
        Activite activite = getActiviteAppartenantA(adminId, activiteId);
        verifierStatutInscriptionOuAttente(activite);

        activite.setDateFinInscription(activite.getDateFinInscription().plus(request.heuresSupplementaires(), ChronoUnit.HOURS));
        activite.setStatut(StatutActivite.INSCRIPTION);
        activite = activiteRepository.save(activite);
        return toResponse(activite);
    }

    // F-13 : demarrer avec les participants deja inscrits (meme si quota non atteint)
    @Transactional
    public ActiviteResponse demarrerAvecParticipantsActuels(Long adminId, Long activiteId) {
        Activite activite = getActiviteAppartenantA(adminId, activiteId);
        verifierStatutInscriptionOuAttente(activite);

        long nbInscrits = participantRepository.countByActiviteId(activiteId);
        if (nbInscrits < 2) {
            throw ApiException.badRequest("Il faut au moins 2 participants inscrits pour demarrer l'activite");
        }

        activite.setDateFinInscription(Instant.now());
        activite.setStatut(StatutActivite.INSCRIPTION);
        activite = activiteRepository.save(activite);
        return toResponse(activite);
    }

    // F-13 : annuler l'activite
    @Transactional
    public ActiviteResponse annulerActivite(Long adminId, Long activiteId) {
        Activite activite = getActiviteAppartenantA(adminId, activiteId);
        if (activite.getStatut() == StatutActivite.CLOTUREE || activite.getStatut() == StatutActivite.ANNULEE) {
            throw ApiException.badRequest("Cette activite est deja terminee");
        }
        activite.setStatut(StatutActivite.ANNULEE);
        activite = activiteRepository.save(activite);
        return toResponse(activite);
    }

    // F-25 : cloture manuelle a tout moment avant l'echeance
    @Transactional
    public ActiviteResponse cloturerActivite(Long adminId, Long activiteId, boolean figerEnLectureSeule) {
        Activite activite = getActiviteAppartenantA(adminId, activiteId);
        if (activite.getStatut() != StatutActivite.EN_COURS && activite.getStatut() != StatutActivite.TIRAGE_EFFECTUE) {
            throw ApiException.badRequest("Seule une activite en cours peut etre cloturee");
        }
        activite.setStatut(StatutActivite.CLOTUREE);
        activite.setDateCloture(Instant.now());
        activite.setLectureSeule(figerEnLectureSeule);
        activite = activiteRepository.save(activite);
        return toResponse(activite);
    }

    // F-05 : archiver/supprimer une activite cloturee
    @Transactional
    public void supprimerActivite(Long adminId, Long activiteId) {
        Activite activite = getActiviteAppartenantA(adminId, activiteId);
        if (activite.getStatut() != StatutActivite.CLOTUREE && activite.getStatut() != StatutActivite.ANNULEE) {
            throw ApiException.badRequest("Seule une activite cloturee ou annulee peut etre supprimee");
        }
        activiteRepository.delete(activite);
    }

    public Activite getActiviteAppartenantA(Long adminId, Long activiteId) {
        Activite activite = activiteRepository.findById(activiteId)
                .orElseThrow(() -> ApiException.notFound("Activite introuvable"));
        if (!activite.getAdmin().getId().equals(adminId)) {
            throw ApiException.forbidden("Cette activite ne vous appartient pas");
        }
        return activite;
    }

    public Activite getActiviteById(Long activiteId) {
        return activiteRepository.findById(activiteId)
                .orElseThrow(() -> ApiException.notFound("Activite introuvable"));
    }

    private void verifierStatutInscriptionOuAttente(Activite activite) {
        if (activite.getStatut() != StatutActivite.INSCRIPTION && activite.getStatut() != StatutActivite.EN_ATTENTE_DECISION) {
            throw ApiException.badRequest("Action impossible : l'activite n'est pas en periode d'inscription");
        }
    }

    public ActiviteResponse toResponse(Activite a) {
        long nbInscrits = participantRepository.countByActiviteId(a.getId());
        return new ActiviteResponse(
                a.getId(), a.getNom(), a.getDescription(), a.getNbParticipantsAttendu(), nbInscrits,
                a.getCodeAcces(), a.getStatut(), a.getDateCreation(), a.getDateDebutInscription(),
                a.getDateFinInscription(), a.getDureeGlobaleJours(), a.getDateDebutActivite(),
                a.getDateFinActivite(), a.getDateCloture(), a.isLectureSeule(), a.getModeConfidentialite()
        );
    }
}
