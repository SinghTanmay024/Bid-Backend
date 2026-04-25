package com.bidbackend.repository;

import com.bidbackend.model.PaymentOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentOrderRepository extends MongoRepository<PaymentOrder, String> {

    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
}
