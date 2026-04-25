package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SocialLoginRequest {

    @NotBlank
    private String idToken;   // Firebase ID token from the frontend
}
