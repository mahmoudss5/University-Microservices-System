package com.uni.iam.controller;

import com.uni.iam.dto.request.LoginRequest;
import com.uni.iam.dto.request.RegisterRequest;
import com.uni.iam.dto.response.AuthResponse;
import com.uni.iam.ratelimit.RateLimit;
import com.uni.iam.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER (PRESENTATION) LAYER
 * Exposes the IAM REST API.
 *
 * Endpoints:
 *   POST /api/auth/register  → register a new user, returns JWT
 *   POST /api/auth/login     → authenticate with email + password, returns JWT
 *
 * This layer only:
 *   • Receives HTTP requests
 *   • Validates input (@Valid)
 *   • Delegates to the service layer
 *   • Maps responses to HTTP status codes
 *
 * NO business logic lives here.
 *
 * Rate Limiting (3-layer defence-in-depth)
 * ─────────────────────────────────────────
 *   Layer 1 — API Gateway GlobalRateLimiterFilter : 150 req/min per IP (all routes)
 *   Layer 2 — API Gateway LuaRateLimiterFactory   : 20  req/min per IP (/api/auth/**)
 *   Layer 3 — @RateLimit AOP (this class)          : 20  req/min per IP (each method)
 *             ↑ configurable via requestsPerMinute attribute
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * Rate limited to {@code requestsPerMinute = 20} by {@link RateLimit}.
     * Adjust the value here to change the service-layer cap independently
     * of the gateway-level filter.
     */
    @PostMapping("/register")
    @RateLimit(requestsPerMinute = 20)
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate an existing user and return a signed JWT.
     *
     * Rate limited to {@code requestsPerMinute = 20} by {@link RateLimit}.
     * Adjust the value here to change the service-layer cap independently
     * of the gateway-level filter.
     */
    @PostMapping("/login")
    @RateLimit(requestsPerMinute = 20)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
