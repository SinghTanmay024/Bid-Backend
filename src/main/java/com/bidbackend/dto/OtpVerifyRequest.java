package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class OtpVerifyRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;
}
