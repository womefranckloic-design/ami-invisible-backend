package com.franck.amiinvisible.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationAdminMs;
    private final long expirationParticipantMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-admin-ms}") long expirationAdminMs,
            @Value("${app.jwt.expiration-participant-ms}") long expirationParticipantMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationAdminMs = expirationAdminMs;
        this.expirationParticipantMs = expirationParticipantMs;
    }

    public String genererTokenAdmin(Long adminId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("type", "ADMIN")
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationAdminMs))
                .signWith(key)
                .compact();
    }

    public String genererTokenParticipant(Long participantId, Long activiteId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(participantId))
                .claim("type", "PARTICIPANT")
                .claim("activiteId", activiteId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationParticipantMs))
                .signWith(key)
                .compact();
    }

    // Point 2 : token du compte participant leger (transverse, sans activiteId)
    public String genererTokenCompteParticipant(Long compteId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(compteId))
                .claim("type", "COMPTE_PARTICIPANT")
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationParticipantMs))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthPrincipal toPrincipal(Claims claims) {
        String type = claims.get("type", String.class);
        Long id = Long.valueOf(claims.getSubject());
        if ("ADMIN".equals(type)) {
            return new AuthPrincipal(AuthPrincipal.TypeCompte.ADMIN, id, null);
        }
        if ("COMPTE_PARTICIPANT".equals(type)) {
            return new AuthPrincipal(AuthPrincipal.TypeCompte.COMPTE_PARTICIPANT, id, null);
        }
        Long activiteId = claims.get("activiteId", Long.class);
        return new AuthPrincipal(AuthPrincipal.TypeCompte.PARTICIPANT, id, activiteId);
    }
}
