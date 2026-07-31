package com.franck.amiinvisible.dto.response;

/**
 * Retourne au participant juste apres son inscription : son identifiant anonyme,
 * son code secret (a conserver precieusement pour se reconnecter, car aucun email/mdp
 * n'est demande) et un token JWT immediatement utilisable.
 */
public record InscriptionParticipantResponse(
        String identifiantAnonyme,
        String codeSecret,
        String token
) {
}
