package com.franck.amiinvisible.dto.response;

/**
 * Vue admin d'une ligne de tirage : correspondance complete avec noms reels (F-16).
 */
public record TirageAdminResponse(
        Long tirageId,
        String offrantNomReel,
        String offrantIdentifiant,
        String destinataireNomReel,
        String destinataireIdentifiant
) {
}
