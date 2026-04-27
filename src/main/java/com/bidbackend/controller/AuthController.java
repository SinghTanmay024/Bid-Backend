package com.bidbackend.controller;

import com.bidbackend.dto.*;
import com.bidbackend.service.AuthService;
import com.bidbackend.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, OTP, forgot-password and social-login endpoints")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /** POST /api/auth/register — { email, password } → { token, email, role } */
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /** POST /api/auth/login — { email, password } → { token, email, role } */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** POST /api/auth/send-otp — { email } → { message: "OTP sent" } */
    @PostMapping("/send-otp")
    @Operation(summary = "Send a 6-digit OTP to the given email")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody OtpRequest request) {
        otpService.sendOtp(request.getEmail(), "BidWin — Your OTP Code");
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP sent"));
    }

    /**
     * POST /api/auth/verify-otp — { email, otp } → { token, email, role }
     * Validates OTP, marks it used, finds-or-creates the user, returns JWT.
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and receive JWT")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyAndConsumeOtp(request.getEmail(), request.getOtp());
        AuthResponse response = authService.loginOrRegisterByEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/forgot-password — { email } → { message: "Reset link sent" }
     * Always returns 200 regardless of whether the email exists.
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password-reset link to the given email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Reset link sent"));
    }

    /** POST /api/auth/social-login — { provider, idToken, email, name } → { token, email, role } */
    @PostMapping("/social-login")
    @Operation(summary = "Login or register via Firebase ID token (Google/Facebook)")
    public ResponseEntity<AuthResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }
}
