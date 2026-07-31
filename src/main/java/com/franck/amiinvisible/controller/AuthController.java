package com.franck.amiinvisible.controller;

import com.franck.amiinvisible.dto.request.LoginAdminRequest;
import com.franck.amiinvisible.dto.request.RegisterAdminRequest;
import com.franck.amiinvisible.dto.response.AuthAdminResponse;
import com.franck.amiinvisible.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthAdminResponse> inscrire(@Valid @RequestBody RegisterAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrireAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthAdminResponse> connecter(@Valid @RequestBody LoginAdminRequest request) {
        return ResponseEntity.ok(authService.connecterAdmin(request));
    }
}
