package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.response.MonTirageResponse;
import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.Participant;
import com.franck.amiinvisible.security.AuthPrincipal;
import com.franck.amiinvisible.security.CurrentUser;
import com.franck.amiinvisible.service.ActiviteService;
import com.franck.amiinvisible.service.ParticipantService;
import com.franck.amiinvisible.service.TirageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/participants/me")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final ActiviteService activiteService;
    private final TirageService tirageService;

    // Infos de base du participant connecte (jamais son propre nom reel n'est un secret,
    // mais on ne renvoie que ce qui est utile cote participant)
    @GetMapping
    public ResponseEntity<Map<String, Object>> monProfil() {
        AuthPrincipal p = CurrentUser.requireParticipant();
        Participant participant = participantService.getParticipantDansActivite(p.id(), p.activiteId());
        Activite activite = activiteService.getActiviteById(p.activiteId());

        return ResponseEntity.ok(Map.of(
                "identifiantAnonyme", participant.getIdentifiantAnonyme(),
                "sexe", participant.getSexe(),
                "activiteId", activite.getId(),
                "activiteNom", activite.getNom(),
                "statutActivite", activite.getStatut()
        ));
    }

    // F-17 : qui je dois offrir / qui m'offre, jamais de nom reel
    @GetMapping("/tirage")
    public ResponseEntity<MonTirageResponse> monTirage() {
        AuthPrincipal p = CurrentUser.requireParticipant();
        return ResponseEntity.ok(tirageService.consulterMonTirage(p.activiteId(), p.id()));
    }
}
