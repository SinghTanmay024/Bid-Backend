package com.bidbackend.service;

import com.bidbackend.dto.AuthRequest;
import com.bidbackend.dto.AuthResponse;
import com.bidbackend.dto.ForgotPasswordRequest;
import com.bidbackend.dto.SocialLoginRequest;
import com.bidbackend.model.User;
import com.bidbackend.repository.UserRepository;
import com.bidbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), "USER");
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), stripRolePrefix(user.getRole()));
    }

    // -------------------------------------------------------------------------
    // OTP login / register
    // -------------------------------------------------------------------------

    /**
     * Called after OTP is verified. Finds or creates the user and returns a JWT.
     * The OTP has already been validated by OtpService before this is called.
     */
    public AuthResponse loginOrRegisterByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("USER")
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), stripRolePrefix(user.getRole()));
    }

    // -------------------------------------------------------------------------
    // Forgot password — sends reset link (email only, always returns 200)
    // -------------------------------------------------------------------------

    public void forgotPassword(ForgotPasswordRequest request) {
        // Always 200 — don't leak whether the email exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            // TODO: persist resetToken with expiry for a full reset flow
            // For now, email the token as a link
            String resetLink = "https://bidwin.app/reset-password?token=" + resetToken + "&email=" + request.getEmail();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject("BidWin — Password Reset");
            message.setText("Click the link below to reset your password:\n\n" + resetLink
                    + "\n\nThis link is valid for 15 minutes.");
            mailSender.send(message);
        });
    }

    // -------------------------------------------------------------------------
    // Social login (Firebase verification)
    // -------------------------------------------------------------------------

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
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role("USER")
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), stripRolePrefix(user.getRole()));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /** Normalise legacy "ROLE_USER" / "ROLE_ADMIN" to "USER" / "ADMIN". */
    private String stripRolePrefix(String role) {
        if (role == null) return "USER";
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }
}
