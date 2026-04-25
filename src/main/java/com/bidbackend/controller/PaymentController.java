package com.bidbackend.controller;

import com.bidbackend.dto.CreateOrderRequest;
import com.bidbackend.dto.CreateOrderResponse;
import com.bidbackend.dto.PaymentVerifyRequest;
import com.bidbackend.dto.WalletResponse;
import com.bidbackend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay order creation and payment verification")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a Razorpay order for a coin purchase")
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature and credit coins to wallet")
    public ResponseEntity<WalletResponse> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndCredit(request));
    }
}
