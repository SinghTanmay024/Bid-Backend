package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SocialLoginRequest {

    @NotBlank
    private String provider;   // "google" or "facebook"

    @NotBlank
    private String idToken;    // Firebase ID token from the frontend

    @NotBlank
    private String email;      // email asserted by the frontend (verified server-side)

    private String name;       // display name (optional, stored if creating a new account)
}
