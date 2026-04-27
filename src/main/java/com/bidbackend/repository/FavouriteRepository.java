package com.bidbackend.repository;

import com.bidbackend.model.Favourite;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends MongoRepository<Favourite, String> {

    List<Favourite> findByUserEmail(String userEmail);

    Optional<Favourite> findByUserEmailAndProductId(String userEmail, String productId);

    boolean existsByUserEmailAndProductId(String userEmail, String productId);

    void deleteByUserEmailAndProductId(String userEmail, String productId);
}
