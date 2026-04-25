package com.bidbackend.service;

import com.bidbackend.model.OtpToken;
import com.bidbackend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final JavaMailSender mailSender;

    /** Generate a 6-digit OTP, persist it, and email it to the user. */
    public void sendOtp(String email, String subject) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Delete any previous OTPs for this email
        otpTokenRepository.deleteByEmail(email);

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();

        otpTokenRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText("Your OTP is: " + otp + "\n\nThis OTP is valid for 10 minutes.");
        mailSender.send(message);
    }

    /**
     * Validate the OTP. Returns true and marks it verified if correct and not expired.
     * Throws IllegalArgumentException on mismatch / expiry.
     */
    public void verifyOtp(String email, String otp) {
        OtpToken token = otpTokenRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found for " + email));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!token.getOtp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        token.setVerified(true);
        otpTokenRepository.save(token);
    }

    /** Verify OTP and then delete it (for password-reset flow). */
    public void verifyAndConsumeOtp(String email, String otp) {
        verifyOtp(email, otp);
        otpTokenRepository.deleteByEmail(email);
    }
}
