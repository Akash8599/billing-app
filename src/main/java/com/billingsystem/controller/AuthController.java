package com.billingsystem.controller;

import com.billingsystem.dto.LoginRequest;
import com.billingsystem.dto.LoginResponse;
import com.billingsystem.dto.RegisterRequest;
import com.billingsystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getUsername());
        try {
            LoginResponse response = authService.login(request);
            log.info("Login successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for user: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                new LoginResponse(null, null, null, "Login failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registration request for user: {}", request.getUsername());
        try {
            LoginResponse response = authService.register(request);
            log.info("User registered successfully: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for user: {}. Reason: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(
                new LoginResponse(null, null, null, "Registration failed: " + e.getMessage())
            );
        }
    }
}
