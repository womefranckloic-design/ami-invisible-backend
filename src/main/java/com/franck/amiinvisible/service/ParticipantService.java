package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.request.InscriptionParticipantRequest;
import com.franck.amiinvisible.dto.request.LoginParticipantRequest;
import com.franck.amiinvisible.dto.response.*;
import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.Participant;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.ActiviteRepository;
import com.franck.amiinvisible.repository.ParticipantRepository;
import com.franck.amiinvisible.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final ActiviteRepository activiteRepository;
    private final CodeGeneratorService codeGeneratorService;
    private final JwtService jwtService;

    // Vue publique de l'activite avant inscription
    public InscriptionActiviteInfoResponse consulterActivitePourInscription(String codeAcces) {
        Activite activite = activiteRepository.findByCodeAcces(codeAcces)
                .orElseThrow(() -> ApiException.notFound("Code d'acces invalide"));

        long nbInscrits = participantRepository.countByActiviteId(activite.getId());
        long placesRestantes = Math.max(0, activite.getNbParticipantsAttendu() - nbInscrits);

        return new InscriptionActiviteInfoResponse(
                activite.getNom(), activite.getDescription(), activite.getStatut(),
                activite.getNbParticipantsAttendu(), nbInscrits, placesRestantes,
                activite.getDateFinInscription()
        );
    }

    // F-09, F-10, F-11, F-12. Point 2 : compteParticipantId optionnel pour lier
    // l'inscription a un compte transverse "mes activites" (null = participation
    // totalement anonyme et isolee, comportement historique inchange).
    @Transactional
    public InscriptionParticipantResponse inscrire(String codeAcces, InscriptionParticipantRequest request, Long compteParticipantId) {
        Activite activite = activiteRepository.findByCodeAcces(codeAcces)
                .orElseThrow(() -> ApiException.notFound("Code d'acces invalide"));

        if (activite.getStatut() != StatutActivite.INSCRIPTION) {
            throw ApiException.badRequest("Les inscriptions ne sont pas (ou plus) ouvertes pour cette activite");
        }
        if (Instant.now().isAfter(activite.getDateFinInscription())) {
            throw ApiException.badRequest("La periode d'inscription est terminee");
        }

        long nbInscrits = participantRepository.countByActiviteId(activite.getId());
        if (nbInscrits >= activite.getNbParticipantsAttendu()) {
            throw ApiException.conflict("Le nombre de participants attendu est deja atteint");
        }

        String identifiantAnonyme;
        do {
            identifiantAnonyme = codeGeneratorService.genererIdentifiantAnonyme();
        } while (participantRepository.existsByIdentifiantAnonyme(identifiantAnonyme));

        String codeSecret = codeGeneratorService.genererCodeSecret();

        Participant.ParticipantBuilder builder = Participant.builder()
                .activite(activite)
                .nomReel(request.nomReel())
                .sexe(request.sexe())
                .identifiantAnonyme(identifiantAnonyme)
                .codeSecret(codeSecret)
                .dateInscription(Instant.now());

        if (compteParticipantId != null) {
            builder.compteParticipant(com.franck.amiinvisible.entity.CompteParticipant.builder().id(compteParticipantId).build());
        }

        Participant participant = participantRepository.save(builder.build());

        String token = jwtService.genererTokenParticipant(participant.getId(), activite.getId());
        return new InscriptionParticipantResponse(identifiantAnonyme, codeSecret, token);
    }

    // Reconnexion d'un participant avec son code secret (aucun email/mdp requis)
    public AuthParticipantResponse connecter(LoginParticipantRequest request) {
        Activite activite = activiteRepository.findByCodeAcces(request.codeAcces())
                .orElseThrow(() -> ApiException.notFound("Code d'acces invalide"));

        Participant participant = participantRepository.findByActiviteIdAndCodeSecret(activite.getId(), request.codeSecret())
                .orElseThrow(() -> ApiException.unauthorized("Code secret invalide"));

        String token = jwtService.genererTokenParticipant(participant.getId(), activite.getId());
        return new AuthParticipantResponse(participant.getIdentifiantAnonyme(), token);
    }

    // F-11, S-04 : vue admin avec noms reels
    public List<ParticipantAdminResponse> listerPourAdmin(Long activiteId) {
        return participantRepository.findByActiviteIdOrderByDateInscriptionAsc(activiteId).stream()
                .map(p -> new ParticipantAdminResponse(p.getId(), p.getNomReel(), p.getSexe(), p.getIdentifiantAnonyme(), p.getDateInscription()))
                .toList();
    }

    public Participant getParticipantDansActivite(Long participantId, Long activiteId) {
        return participantRepository.findByIdAndActiviteId(participantId, activiteId)
                .orElseThrow(() -> ApiException.notFound("Participant introuvable"));
    }

    // Point 1 : l'admin peut regenerer un code secret perdu et le transmettre en direct
    // au participant (aucun canal automatique - hors perimetre V1).
    @Transactional
    public NouveauCodeSecretResponse regenererCodeSecret(Long activiteId, Long participantId) {
        Participant participant = getParticipantDansActivite(participantId, activiteId);
        String nouveauCode = codeGeneratorService.genererCodeSecret();
        participant.setCodeSecret(nouveauCode);
        participant = participantRepository.save(participant);
        // On renvoie le nouveau code secret a l'admin, jamais stocke en clair ailleurs ni journalise.
        return new NouveauCodeSecretResponse(participant.getIdentifiantAnonyme(), nouveauCode);
    }

    public ParticipantPublicResponse toPublicResponse(Participant p) {
        return new ParticipantPublicResponse(p.getIdentifiantAnonyme(), p.getSexe());
    }

    // Point 2 : vue agregee "mes activites" - lecture seule, ne renvoie que ce que
    // le participant sait deja de lui-meme, jamais de donnee d'une autre activite.
    public List<ParticipationResponse> listerMesParticipations(Long compteParticipantId) {
        return participantRepository.findByCompteParticipantIdOrderByDateInscriptionDesc(compteParticipantId).stream()
                .map(p -> new ParticipationResponse(
                        p.getId(), p.getActivite().getId(), p.getActivite().getNom(),
                        p.getActivite().getStatut(), p.getIdentifiantAnonyme(), p.getDateInscription()
                ))
                .toList();
    }

    // Point 2 : emet un token PARTICIPANT scope a une activite, uniquement si la
    // participation appartient bien au compte authentifie (verification d'appartenance).
    public AuthParticipantResponse genererTokenPourParticipation(Long compteParticipantId, Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> ApiException.notFound("Participation introuvable"));

        if (participant.getCompteParticipant() == null || !participant.getCompteParticipant().getId().equals(compteParticipantId)) {
            throw ApiException.forbidden("Cette participation n'appartient pas a votre compte");
        }

        String token = jwtService.genererTokenParticipant(participant.getId(), participant.getActivite().getId());
        return new AuthParticipantResponse(participant.getIdentifiantAnonyme(), token);
    }
}
