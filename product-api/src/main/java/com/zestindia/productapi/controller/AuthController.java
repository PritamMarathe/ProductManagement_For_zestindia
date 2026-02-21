
package com.zestindia.productapi.controller;

import com.zestindia.productapi.dto.AuthRequest;
import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.RefreshRequest;
import com.zestindia.productapi.entity.User;
import com.zestindia.productapi.service.JwtService;
import com.zestindia.productapi.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails user = userService.loadUserByUsername(request.getUsername());
            String access = jwtService.generateToken(user);
            String refresh = jwtService.generateRefreshToken(user);
            AuthResponse resp = new AuthResponse();
            resp.setAccessToken(access);
            resp.setRefreshToken(refresh);
            return ResponseEntity.ok(resp);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(jwtService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());  // will be encoded in service
        user.setRole("USER");                     // or "ADMIN"
        User saved = userService.register(user);
        return ResponseEntity.ok(saved);
    }
}