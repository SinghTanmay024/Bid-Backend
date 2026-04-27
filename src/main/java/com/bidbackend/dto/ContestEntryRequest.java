package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ContestEntryRequest {

    @NotBlank(message = "userId is required")
    private String userId;     // email

    private String referredBy; // email of referrer, nullable
}
