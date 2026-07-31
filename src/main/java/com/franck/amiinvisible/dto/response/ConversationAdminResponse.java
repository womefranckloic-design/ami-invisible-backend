package com.franck.amiinvisible.dto.response;

public record ConversationAdminResponse(
        Long conversationId,
        String offrantNomReel,
        String offrantIdentifiant,
        String destinataireNomReel,
        String destinataireIdentifiant,
        long nbMessages
) {
}
