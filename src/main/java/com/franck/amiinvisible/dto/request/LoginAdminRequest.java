package com.franck.amiinvisible.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginAdminRequest(
        @NotBlank @Email(message = "email invalide") String email,
        @NotBlank String motDePasse
) {
}
