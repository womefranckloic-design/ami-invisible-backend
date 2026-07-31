package com.franck.amiinvisible.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterAdminRequest(
        @NotBlank(message = "le nom est obligatoire") String nom,
        @NotBlank @Email(message = "email invalide") String email,
        @NotBlank @Size(min = 8, message = "le mot de passe doit contenir au moins 8 caracteres") String motDePasse
) {
}
