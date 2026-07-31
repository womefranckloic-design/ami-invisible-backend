package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.request.LoginCompteParticipantRequest;
import com.franck.amiinvisible.dto.request.RegisterCompteParticipantRequest;
import com.franck.amiinvisible.dto.response.AuthCompteParticipantResponse;
import com.franck.amiinvisible.dto.response.AuthParticipantResponse;
import com.franck.amiinvisible.dto.response.ParticipationResponse;
import com.franck.amiinvisible.security.AuthPrincipal;
import com.franck.amiinvisible.security.CurrentUser;
import com.franck.amiinvisible.service.CompteParticipantService;
import com.franck.amiinvisible.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Point 2 : compte participant leger, transverse a toutes les activites d'un
 * meme utilisateur, sans jamais fusionner les donnees d'activite entre elles.
 */
@RestController
@RequiredArgsConstructor
public class CompteParticipantController {

    private final CompteParticipantService compteParticipantService;
    private final ParticipantService participantService;

    @PostMapping("/api/auth/participant/register")
    public ResponseEntity<AuthCompteParticipantResponse> inscrire(@Valid @RequestBody RegisterCompteParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compteParticipantService.inscrire(request));
    }

    @PostMapping("/api/auth/participant/login")
    public ResponseEntity<AuthCompteParticipantResponse> connecter(@Valid @RequestBody LoginCompteParticipantRequest request) {
        return ResponseEntity.ok(compteParticipantService.connecter(request));
    }

    // Vue "mes activites" : toutes les participations liees a ce compte
    @GetMapping("/api/comptes/me/participations")
    public ResponseEntity<List<ParticipationResponse>> mesParticipations() {
        AuthPrincipal compte = CurrentUser.requireCompteParticipant();
        return ResponseEntity.ok(participantService.listerMesParticipations(compte.id()));
    }

    // Emet un token PARTICIPANT scope a une activite precise, pour ouvrir cette
    // participation depuis la vue "mes activites"
    @PostMapping("/api/comptes/me/participations/{participantId}/token")
    public ResponseEntity<AuthParticipantResponse> obtenirTokenParticipation(@PathVariable Long participantId) {
        AuthPrincipal compte = CurrentUser.requireCompteParticipant();
        return ResponseEntity.ok(participantService.genererTokenPourParticipation(compte.id(), participantId));
    }
}
