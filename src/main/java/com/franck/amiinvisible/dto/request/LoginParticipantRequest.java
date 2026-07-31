package com.franck.amiinvisible.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginParticipantRequest(
        @NotBlank(message = "le code d'acces de l'activite est obligatoire") String codeAcces,
        @NotBlank(message = "le code secret est obligatoire") String codeSecret
) {
}
