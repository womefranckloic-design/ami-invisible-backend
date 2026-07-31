package com.franck.amiinvisible.dto.response;

import java.time.Instant;

/**
 * Vue admin d'un message en supervision (F-22) : nom reel de l'expediteur visible.
 */
public record MessageAdminResponse(
        Long id,
        String expediteurNomReel,
        String expediteurIdentifiant,
        String contenu,
        Instant dateEnvoi
) {
}
