package com.billingsystem.service;

import com.billingsystem.dto.LoginRequest;
import com.billingsystem.dto.LoginResponse;
import com.billingsystem.dto.RegisterRequest;
import com.billingsystem.model.User;
import com.billingsystem.repository.UserRepository;
import com.billingsystem.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getActive()) {
            throw new RuntimeException("User account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtProvider.generateToken(user.getUsername(), user.getRole(), user.getId());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "Login successful");
    }

    public LoginResponse register(RegisterRequest request) {
        // Validation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "ADMIN");
        user.setActive(true);
        user.setCreatedAt(System.currentTimeMillis());
        // user.setUpdatedAt(System.currentTimeMillis());
        log.info("Saving new user to database: {}", user.getUsername());
        userRepository.save(user);

        String token = jwtProvider.generateToken(user.getUsername(), user.getRole(), user.getId());

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "Registration successful");
    }
}
