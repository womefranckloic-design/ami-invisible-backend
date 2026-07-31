package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.request.CreerActiviteRequest;
import com.franck.amiinvisible.dto.request.ProlongerInscriptionRequest;
import com.franck.amiinvisible.dto.response.*;
import com.franck.amiinvisible.security.AuthPrincipal;
import com.franck.amiinvisible.security.CurrentUser;
import com.franck.amiinvisible.service.ActiviteService;
import com.franck.amiinvisible.service.ConversationService;
import com.franck.amiinvisible.service.ParticipantService;
import com.franck.amiinvisible.service.TirageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activites")
@RequiredArgsConstructor
public class ActiviteController {

    private final ActiviteService activiteService;
    private final ParticipantService participantService;
    private final TirageService tirageService;
    private final ConversationService conversationService;

    // F-01
    @PostMapping
    public ResponseEntity<ActiviteResponse> creer(@Valid @RequestBody CreerActiviteRequest request) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(activiteService.creerActivite(admin.id(), request));
    }

    // F-03
    @GetMapping
    public ResponseEntity<List<ActiviteResponse>> lister() {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.listerMesActivites(admin.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteResponse> consulter(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.consulterActivite(admin.id(), id));
    }

    // F-13
    @PatchMapping("/{id}/prolonger")
    public ResponseEntity<ActiviteResponse> prolonger(@PathVariable Long id, @Valid @RequestBody ProlongerInscriptionRequest request) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.prolongerInscription(admin.id(), id, request));
    }

    // F-13
    @PostMapping("/{id}/demarrer-maintenant")
    public ResponseEntity<ActiviteResponse> demarrerMaintenant(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.demarrerAvecParticipantsActuels(admin.id(), id));
    }

    // F-13
    @PostMapping("/{id}/annuler")
    public ResponseEntity<ActiviteResponse> annuler(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.annulerActivite(admin.id(), id));
    }

    // F-25, F-26
    @PostMapping("/{id}/cloturer")
    public ResponseEntity<ActiviteResponse> cloturer(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "true") boolean lectureSeule) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(activiteService.cloturerActivite(admin.id(), id, lectureSeule));
    }

    // F-05
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        activiteService.supprimerActivite(admin.id(), id);
        return ResponseEntity.noContent().build();
    }

    // F-11, S-04 : liste des participants avec noms reels
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantAdminResponse>> listerParticipants(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        activiteService.getActiviteAppartenantA(admin.id(), id); // verifie la propriete
        return ResponseEntity.ok(participantService.listerPourAdmin(id));
    }

    // Point 1 : regenerer le code secret d'un participant qui a perdu son acces
    @PostMapping("/{id}/participants/{participantId}/regenerer-code")
    public ResponseEntity<NouveauCodeSecretResponse> regenererCode(@PathVariable Long id, @PathVariable Long participantId) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        activiteService.getActiviteAppartenantA(admin.id(), id); // verifie la propriete
        return ResponseEntity.ok(participantService.regenererCodeSecret(id, participantId));
    }

    // F-14 a F-18
    @PostMapping("/{id}/tirage")
    public ResponseEntity<List<TirageAdminResponse>> lancerTirage(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(tirageService.lancerTirage(admin.id(), id));
    }

    @GetMapping("/{id}/tirage")
    public ResponseEntity<List<TirageAdminResponse>> consulterTirage(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(tirageService.consulterTirageAdmin(admin.id(), id));
    }

    // F-18 : reinitialisation explicite (destructive)
    @PostMapping("/{id}/tirage/reinitialiser")
    public ResponseEntity<Void> reinitialiserTirage(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        tirageService.reinitialiserTirage(admin.id(), id);
        return ResponseEntity.noContent().build();
    }

    // F-22 : supervision des conversations
    @GetMapping("/{id}/conversations")
    public ResponseEntity<List<ConversationAdminResponse>> listerConversations(@PathVariable Long id) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(conversationService.listerPourAdmin(admin.id(), id));
    }

    @GetMapping("/{id}/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageAdminResponse>> consulterMessagesAdmin(@PathVariable Long id, @PathVariable Long conversationId) {
        AuthPrincipal admin = CurrentUser.requireAdmin();
        return ResponseEntity.ok(conversationService.consulterMessagesAdmin(admin.id(), id, conversationId));
    }
}
