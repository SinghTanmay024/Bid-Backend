package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Spec: POST /api/auth/forgot-password
 * Body: { email }
 * Response: { message: "Reset link sent" }
 * Always returns 200 regardless of whether the email exists.
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    private String email;
}
