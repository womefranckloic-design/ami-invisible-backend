package com.franck.amiinvisible.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnvoyerMessageRequest(
        @NotBlank(message = "le message ne peut pas etre vide")
        @Size(max = 4000, message = "message trop long (4000 caracteres max)")
        String contenu
) {
}
