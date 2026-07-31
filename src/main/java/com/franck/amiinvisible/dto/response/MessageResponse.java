package com.franck.amiinvisible.dto.response;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long expediteurParticipantId,
        boolean envoyeParMoi,
        String contenu,
        Instant dateEnvoi
) {
}
