package com.bidbackend.repository;

import com.bidbackend.model.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends MongoRepository<Bid, String> {

    List<Bid> findByProductId(String productId);

    List<Bid> findByUserId(String userId);

    Optional<Bid> findByUserIdAndProductId(String userId, String productId);

    boolean existsByUserIdAndProductId(String userId, String productId);
}
