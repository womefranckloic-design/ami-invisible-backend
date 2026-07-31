package com.franck.amiinvisible.dto.response;

public record ConversationResponse(
        Long conversationId,
        String role,          // "OFFRANT" ou "DESTINATAIRE" du point de vue du participant courant
        ParticipantPublicResponse interlocuteur,
        long nbNonLus
) {
}
