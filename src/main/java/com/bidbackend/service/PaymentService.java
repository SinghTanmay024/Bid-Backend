package com.bidbackend.service;

import com.bidbackend.dto.CreateOrderRequest;
import com.bidbackend.dto.CreateOrderResponse;
import com.bidbackend.dto.PaymentVerifyRequest;
import com.bidbackend.dto.WalletResponse;
import com.bidbackend.model.PaymentOrder;
import com.bidbackend.repository.PaymentOrderRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final WalletService walletService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmountPaise());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "bidwin_" + request.getUserId() + "_" + System.currentTimeMillis());

            Order razorpayOrder = client.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // Persist the order so we can verify it later
            PaymentOrder order = PaymentOrder.builder()
                    .razorpayOrderId(razorpayOrderId)
                    .userId(request.getUserId())
                    .amountPaise(request.getAmountPaise())
                    .coins(request.getCoins())
                    .status("CREATED")
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentOrderRepository.save(order);

            return new CreateOrderResponse(razorpayOrderId, request.getAmountPaise(), "INR", razorpayKeyId);

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    public WalletResponse verifyAndCredit(PaymentVerifyRequest request) {
        // 1. Look up our stored order
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getRazorpayOrderId()));

        if ("PAID".equals(order.getStatus())) {
            throw new IllegalStateException("Payment already processed for order: " + request.getRazorpayOrderId());
        }

        // 2. Verify HMAC-SHA256 signature
        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String expectedSignature = hmacSha256(payload, razorpayKeySecret);

        if (!expectedSignature.equals(request.getRazorpaySignature())) {
            order.setStatus("FAILED");
            paymentOrderRepository.save(order);
            throw new IllegalArgumentException("Payment signature verification failed.");
        }

        // 3. Mark order as paid and credit coins
        order.setStatus("PAID");
        paymentOrderRepository.save(order);

        return walletService.purchaseCoins(request.getUserId(), order.getCoins());
    }

    // -------------------------------------------------------------------------

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }
}
