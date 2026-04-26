package com.rfq.system.controller;

import com.rfq.system.dto.auth.LoginRequest;
import com.rfq.system.dto.auth.LoginResponse;
import com.rfq.system.dto.response.ApiResponse;
import com.rfq.system.entity.User;
import com.rfq.system.repository.UserRepository;
import com.rfq.system.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping({"/auth/login", "/api/auth/login"})
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        LoginResponse loginResponse = new LoginResponse(token, user.getUsername(), user.getRole(), user.getId());
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }
}
