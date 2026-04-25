package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String userId;

    /** Number of coins the user wants to buy. */
    @NotNull
    @Min(1)
    private Integer coins;

    /**
     * Amount in paise (INR). E.g. 10 coins = 1000 paise = ₹10.
     * The frontend should calculate this based on the coin package selected.
     */
    @NotNull
    @Min(100)
    private Integer amountPaise;
}
