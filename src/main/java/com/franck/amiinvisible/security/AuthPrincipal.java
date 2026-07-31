package com.franck.amiinvisible.security;

/**
 * Principal porte par le contexte de securite apres validation du JWT.
 * type = ADMIN ou PARTICIPANT.
 * Pour un participant, activiteId permet de garantir le cloisonnement (S-01)
 * sans avoir a re-questionner la base a chaque requete.
 */
public record AuthPrincipal(TypeCompte type, Long id, Long activiteId) {

    public enum TypeCompte {
        ADMIN,
        PARTICIPANT,
        // Point 2 : compte leger transverse, ne porte aucune donnee d'activite
        COMPTE_PARTICIPANT
    }

    public boolean isAdmin() {
        return type == TypeCompte.ADMIN;
    }

    public boolean isParticipant() {
        return type == TypeCompte.PARTICIPANT;
    }

    public boolean isCompteParticipant() {
        return type == TypeCompte.COMPTE_PARTICIPANT;
    }
}
