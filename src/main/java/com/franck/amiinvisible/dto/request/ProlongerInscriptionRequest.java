package com.franck.amiinvisible.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProlongerInscriptionRequest(
        @NotNull @Min(value = 1, message = "la prolongation doit etre d'au moins 1 heure") Integer heuresSupplementaires
) {
}
