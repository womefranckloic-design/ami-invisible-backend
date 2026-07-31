package com.franck.amiinvisible.dto.response;

import com.franck.amiinvisible.entity.enums.Sexe;

import java.time.Instant;

/**
 * Vue reservee a l'administrateur : contient le nom reel (F-11, S-04).
 */
public record ParticipantAdminResponse(
        Long id,
        String nomReel,
        Sexe sexe,
        String identifiantAnonyme,
        Instant dateInscription
) {
}
