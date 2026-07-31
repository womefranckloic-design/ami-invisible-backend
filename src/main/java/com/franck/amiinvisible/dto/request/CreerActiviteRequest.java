package com.franck.amiinvisible.dto.request;

import com.franck.amiinvisible.entity.enums.ModeConfidentialite;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreerActiviteRequest(
        @NotBlank(message = "le nom de l'activite est obligatoire") String nom,
        String description,
        // Point 3 : minimum releve a 5 pour limiter la devinabilite en petit groupe
        @NotNull @Min(value = 5, message = "il faut au moins 5 participants pour preserver l'anonymat") Integer nbParticipantsAttendu,
        @NotNull @Min(value = 1, message = "la duree d'inscription doit etre d'au moins 1 heure") Integer dureeInscriptionHeures,
        @NotNull @Min(value = 1, message = "la duree globale doit etre d'au moins 1 jour") Integer dureeGlobaleJours,
        // Point 5 : optionnel, defaut SUPERVISION_TOTALE cote service si absent
        ModeConfidentialite modeConfidentialite
) {
}
