package com.franck.amiinvisible.dto.response;

import com.franck.amiinvisible.entity.enums.StatutActivite;

import java.time.Instant;

/**
 * Vue publique d'une activite consultee via son code d'acces (avant inscription).
 * Ne contient aucune donnee personnelle.
 */
public record InscriptionActiviteInfoResponse(
        String nom,
        String description,
        StatutActivite statut,
        int nbParticipantsAttendu,
        long nbParticipantsInscrits,
        long placesRestantes,
        Instant dateFinInscription
) {
}
