package com.franck.amiinvisible.dto.response;

public record AuthCompteParticipantResponse(
        Long id,
        String email,
        String token
) {
}
