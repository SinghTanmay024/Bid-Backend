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
@Tag(name = "Authentication", description = "Register, login, OTP and social-login endpoints")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send a 6-digit OTP to the given email")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody OtpRequest request) {
        otpService.sendOtp(request.getEmail(), "BidWin — Your OTP Code");
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP sent to " + request.getEmail()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify the OTP sent to email")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Collections.singletonMap("message", "OTP verified successfully."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Reset password using OTP (call send-otp first)")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Password updated successfully."));
    }

    @PostMapping("/social-login")
    @Operation(summary = "Login or register via Firebase ID token (Google/Facebook)")
    public ResponseEntity<AuthResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }
}
