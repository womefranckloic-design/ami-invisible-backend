package com.franck.amiinvisible.dto.response;

/**
 * Point 1 : reponse a la regeneration d'un code secret perdu par l'admin.
 * A transmettre en direct au participant (WhatsApp, SMS manuel...), jamais journalise en clair.
 */
public record NouveauCodeSecretResponse(
        String identifiantAnonyme,
        String nouveauCodeSecret
) {
}
