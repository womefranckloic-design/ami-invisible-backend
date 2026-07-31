package com.franck.amiinvisible.dto.request;

import com.franck.amiinvisible.entity.enums.Sexe;
import jakarta.validation.constraints.NotBlank;

// Point 3 (arbitrage Franck) : le sexe devient optionnel pour reduire la devinabilite
// du tirage dans les petits groupes.
public record InscriptionParticipantRequest(
        @NotBlank(message = "le nom reel est obligatoire") String nomReel,
        Sexe sexe
) {
}
