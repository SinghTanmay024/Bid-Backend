package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CoinPurchaseRequest {

    @NotNull
    @Min(1)
    private Integer coins;   // number of coins to credit
}
