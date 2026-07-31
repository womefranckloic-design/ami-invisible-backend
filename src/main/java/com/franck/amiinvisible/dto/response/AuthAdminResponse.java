package com.franck.amiinvisible.dto.response;

public record AuthAdminResponse(
        Long id,
        String nom,
        String email,
        String token
) {
}
