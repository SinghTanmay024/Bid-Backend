package com.bidbackend.repository;

import com.bidbackend.model.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String userId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByEmail(String email);
}
