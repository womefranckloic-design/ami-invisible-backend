package com.franck.amiinvisible.dto.response;

import com.franck.amiinvisible.entity.enums.StatutActivite;

import java.time.Instant;

/**
 * Point 2 : une ligne de la vue "mes activites" d'un compte participant.
 * Ne contient que ce que le participant connait deja de lui-meme (pas de fuite
 * entre activites : chaque ligne correspond a SA propre inscription).
 */
public record ParticipationResponse(
        Long participantId,
        Long activiteId,
        String activiteNom,
        StatutActivite statutActivite,
        String identifiantAnonyme,
        Instant dateInscription
) {
}
