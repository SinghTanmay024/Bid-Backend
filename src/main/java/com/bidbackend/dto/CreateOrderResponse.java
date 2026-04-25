package com.bidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {

    private String orderId;       // Razorpay order ID — pass to Razorpay checkout
    private Integer amountPaise;
    private String currency;
    private String keyId;         // Razorpay key ID — needed by the frontend SDK
}
