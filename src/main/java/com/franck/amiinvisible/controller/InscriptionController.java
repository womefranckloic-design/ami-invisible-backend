package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.request.InscriptionParticipantRequest;
import com.franck.amiinvisible.dto.request.LoginParticipantRequest;
import com.franck.amiinvisible.dto.response.AuthParticipantResponse;
import com.franck.amiinvisible.dto.response.InscriptionActiviteInfoResponse;
import com.franck.amiinvisible.dto.response.InscriptionParticipantResponse;
import com.franck.amiinvisible.security.AuthPrincipal;
import com.franck.amiinvisible.security.CurrentUser;
import com.franck.amiinvisible.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints publics (sans authentification obligatoire) utilises par un futur participant
 * a partir du lien/code d'acces transmis par l'administrateur (F-08, F-09).
 *
 * Point 2 : si un token de compte participant (Bearer) est fourni en en-tete au moment de
 * l'inscription, la participation est automatiquement rattachee a ce compte pour apparaitre
 * dans la vue "mes activites" - c'est facultatif, l'inscription anonyme classique reste possible.
 */
@RestController
@RequestMapping("/api/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final ParticipantService participantService;

    @GetMapping("/{codeAcces}")
    public ResponseEntity<InscriptionActiviteInfoResponse> consulterActivite(@PathVariable String codeAcces) {
        return ResponseEntity.ok(participantService.consulterActivitePourInscription(codeAcces));
    }

    @PostMapping("/{codeAcces}")
    public ResponseEntity<InscriptionParticipantResponse> inscrire(
            @PathVariable String codeAcces,
            @Valid @RequestBody InscriptionParticipantRequest request) {

        Long compteParticipantId = CurrentUser.getOptional()
                .filter(AuthPrincipal::isCompteParticipant)
                .map(AuthPrincipal::id)
                .orElse(null);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participantService.inscrire(codeAcces, request, compteParticipantId));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthParticipantResponse> connecter(@Valid @RequestBody LoginParticipantRequest request) {
        return ResponseEntity.ok(participantService.connecter(request));
    }
}
