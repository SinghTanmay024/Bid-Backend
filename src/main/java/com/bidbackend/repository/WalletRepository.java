package com.bidbackend.repository;

import com.bidbackend.model.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WalletRepository extends MongoRepository<Wallet, String> {

    Optional<Wallet> findByUserId(String userId);
}
