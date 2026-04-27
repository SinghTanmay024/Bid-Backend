package com.bidbackend.service;

import com.bidbackend.model.Favourite;
import com.bidbackend.repository.FavouriteRepository;
import com.bidbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final ProductRepository productRepository;

    public List<String> getFavourites(String userEmail) {
        return favouriteRepository.findByUserEmail(userEmail)
                .stream()
                .map(Favourite::getProductId)
                .collect(Collectors.toList());
    }

    public void addFavourite(String userEmail, String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }
        // Idempotent — do nothing if already favourited
        if (!favouriteRepository.existsByUserEmailAndProductId(userEmail, productId)) {
            Favourite favourite = Favourite.builder()
                    .userEmail(userEmail)
                    .productId(productId)
                    .build();
            favouriteRepository.save(favourite);
        }
    }

    @Transactional
    public void removeFavourite(String userEmail, String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId);
        }
        favouriteRepository.deleteByUserEmailAndProductId(userEmail, productId);
    }
}
