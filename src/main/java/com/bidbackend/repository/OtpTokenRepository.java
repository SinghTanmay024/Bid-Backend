package com.bidbackend.repository;

import com.bidbackend.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findTopByEmailOrderByExpiresAtDesc(String email);

    void deleteByEmail(String email);
}
