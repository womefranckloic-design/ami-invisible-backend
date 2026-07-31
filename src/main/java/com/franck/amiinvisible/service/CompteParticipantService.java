package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.request.LoginCompteParticipantRequest;
import com.franck.amiinvisible.dto.request.RegisterCompteParticipantRequest;
import com.franck.amiinvisible.dto.response.AuthCompteParticipantResponse;
import com.franck.amiinvisible.entity.CompteParticipant;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.CompteParticipantRepository;
import com.franck.amiinvisible.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Point 2 (arbitrage Franck) : compte participant leger et optionnel, uniquement
 * utilise pour la vue "mes activites" transverse. Ne remplace jamais le
 * codeSecret par activite (S-01 reste intact).
 */
@Service
@RequiredArgsConstructor
public class CompteParticipantService {

    private final CompteParticipantRepository compteParticipantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthCompteParticipantResponse inscrire(RegisterCompteParticipantRequest request) {
        if (compteParticipantRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Un compte participant existe deja avec cet email");
        }
        CompteParticipant compte = CompteParticipant.builder()
                .email(request.email())
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .build();
        compte = compteParticipantRepository.save(compte);

        String token = jwtService.genererTokenCompteParticipant(compte.getId(), compte.getEmail());
        return new AuthCompteParticipantResponse(compte.getId(), compte.getEmail(), token);
    }

    public AuthCompteParticipantResponse connecter(LoginCompteParticipantRequest request) {
        CompteParticipant compte = compteParticipantRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Identifiants invalides"));

        if (!passwordEncoder.matches(request.motDePasse(), compte.getMotDePasseHash())) {
            throw ApiException.unauthorized("Identifiants invalides");
        }

        String token = jwtService.genererTokenCompteParticipant(compte.getId(), compte.getEmail());
        return new AuthCompteParticipantResponse(compte.getId(), compte.getEmail(), token);
    }
}
