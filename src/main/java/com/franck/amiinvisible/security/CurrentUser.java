package com.franck.amiinvisible.security;

import com.franck.amiinvisible.exception.ApiException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthPrincipal get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw ApiException.unauthorized("Authentification requise");
        }
        return authPrincipal;
    }

    // Utile sur les endpoints publics ou l'authentification est facultative
    // (ex : inscription liee a un compte participant si un token est fourni, F-08/Point 2)
    public static Optional<AuthPrincipal> getOptional() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        return principal instanceof AuthPrincipal authPrincipal ? Optional.of(authPrincipal) : Optional.empty();
    }

    public static AuthPrincipal requireAdmin() {
        AuthPrincipal p = get();
        if (!p.isAdmin()) {
            throw ApiException.forbidden("Reserve aux administrateurs");
        }
        return p;
    }

    public static AuthPrincipal requireParticipant() {
        AuthPrincipal p = get();
        if (!p.isParticipant()) {
            throw ApiException.forbidden("Reserve aux participants");
        }
        return p;
    }

    public static AuthPrincipal requireCompteParticipant() {
        AuthPrincipal p = get();
        if (!p.isCompteParticipant()) {
            throw ApiException.forbidden("Reserve aux comptes participants");
        }
        return p;
    }
}

