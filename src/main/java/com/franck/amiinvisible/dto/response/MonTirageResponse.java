package com.franck.amiinvisible.dto.response;

/**
 * Ce que voit un participant : uniquement l'identifiant/sexe de qui il offre
 * et de qui lui offre, jamais de nom reel (F-17).
 */
public record MonTirageResponse(
        ParticipantPublicResponse jOffreA,
        ParticipantPublicResponse quiMOffre
) {
}
