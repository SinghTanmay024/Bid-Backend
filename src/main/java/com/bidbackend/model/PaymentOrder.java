package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "payment_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {

    @Id
    private String id;

    @Indexed(unique = true)
    private String razorpayOrderId;

    private String userId;

    private Integer amountPaise;   // amount in paise (INR smallest unit)

    private Integer coins;         // coins to credit on successful payment

    private String status;         // CREATED, PAID, FAILED

    private LocalDateTime createdAt;
}
