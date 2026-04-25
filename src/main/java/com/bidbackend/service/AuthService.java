package com.bidbackend.service;

import com.bidbackend.dto.AuthRequest;
import com.bidbackend.dto.AuthResponse;
import com.bidbackend.dto.ForgotPasswordRequest;
import com.bidbackend.dto.SocialLoginRequest;
import com.bidbackend.model.User;
import com.bidbackend.repository.UserRepository;
import com.bidbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account found for " + request.getEmail()));

        otpService.verifyAndConsumeOtp(request.getEmail(), request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @SuppressWarnings("unchecked")
    public AuthResponse socialLogin(SocialLoginRequest request) {
        // Verify the Firebase ID token via Google's tokeninfo endpoint
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> tokenInfo;
        try {
            tokenInfo = restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Firebase ID token.");
        }

        if (tokenInfo == null || tokenInfo.containsKey("error")) {
            throw new IllegalArgumentException("Invalid Firebase ID token.");
        }

        String email = (String) tokenInfo.get("email");
        if (email == null) {
            throw new IllegalArgumentException("Email not present in token.");
        }

        // Upsert: find existing user or create one (no password for social accounts)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .role("ROLE_USER")
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
    }
}
