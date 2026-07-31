package com.franck.amiinvisible.service;

import com.franck.amiinvisible.dto.request.LoginAdminRequest;
import com.franck.amiinvisible.dto.request.RegisterAdminRequest;
import com.franck.amiinvisible.dto.response.AuthAdminResponse;
import com.franck.amiinvisible.entity.Admin;
import com.franck.amiinvisible.exception.ApiException;
import com.franck.amiinvisible.repository.AdminRepository;
import com.franck.amiinvisible.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthAdminResponse inscrireAdmin(RegisterAdminRequest request) {
        if (adminRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Un compte existe deja avec cet email");
        }
        Admin admin = Admin.builder()
                .nom(request.nom())
                .email(request.email())
                .motDePasseHash(passwordEncoder.encode(request.motDePasse()))
                .build();
        admin = adminRepository.save(admin);

        String token = jwtService.genererTokenAdmin(admin.getId(), admin.getEmail());
        return new AuthAdminResponse(admin.getId(), admin.getNom(), admin.getEmail(), token);
    }

    public AuthAdminResponse connecterAdmin(LoginAdminRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Identifiants invalides"));

        if (!passwordEncoder.matches(request.motDePasse(), admin.getMotDePasseHash())) {
            throw ApiException.unauthorized("Identifiants invalides");
        }

        String token = jwtService.genererTokenAdmin(admin.getId(), admin.getEmail());
        return new AuthAdminResponse(admin.getId(), admin.getNom(), admin.getEmail(), token);
    }
}
