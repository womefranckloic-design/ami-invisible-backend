package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.request.EnvoyerMessageRequest;
import com.franck.amiinvisible.dto.response.ConversationResponse;
import com.franck.amiinvisible.dto.response.MessageResponse;
import com.franck.amiinvisible.security.AuthPrincipal;
import com.franck.amiinvisible.security.CurrentUser;
import com.franck.amiinvisible.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * F-19, F-20, F-21 : chaque participant n'accede qu'a ses deux fils de discussion,
 * sans jamais voir le nom reel de son interlocuteur.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> mesConversations() {
        AuthPrincipal p = CurrentUser.requireParticipant();
        return ResponseEntity.ok(conversationService.listerMesConversations(p.activiteId(), p.id()));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> messages(@PathVariable Long conversationId) {
        AuthPrincipal p = CurrentUser.requireParticipant();
        return ResponseEntity.ok(conversationService.consulterMessages(p.activiteId(), p.id(), conversationId));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<MessageResponse> envoyer(@PathVariable Long conversationId,
                                                    @Valid @RequestBody EnvoyerMessageRequest request) {
        AuthPrincipal p = CurrentUser.requireParticipant();
        MessageResponse reponse = conversationService.envoyerMessage(p.activiteId(), p.id(), conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }
}
