package com.franck.amiinvisible.dto.response;

import com.franck.amiinvisible.entity.enums.Sexe;

/**
 * Vue restreinte transmise a un autre participant : jamais de nom reel (F-17, F-21, S-02).
 */
public record ParticipantPublicResponse(
        String identifiantAnonyme,
        Sexe sexe
) {
}
