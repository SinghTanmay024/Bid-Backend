package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PaymentVerifyRequest {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;

    @NotBlank
    private String userId;
}
