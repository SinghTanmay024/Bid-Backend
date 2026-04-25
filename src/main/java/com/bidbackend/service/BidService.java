package com.bidbackend.service;

import com.bidbackend.dto.BidResponse;
import com.bidbackend.dto.ProductResponse;
import com.bidbackend.model.Bid;
import com.bidbackend.model.Product;
import com.bidbackend.repository.BidRepository;
import com.bidbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

// WalletService is injected to deduct coins when a bid is placed

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final WalletService walletService;

    public BidResponse placeBid(String productId, String userId) {
        // Validate product exists and is OPEN
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (product.getStatus() == Product.ProductStatus.COMPLETED) {
            throw new IllegalStateException("Bidding is already closed for this product.");
        }

        // Each user can bid only once per product
        if (bidRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("You have already placed a bid on this product.");
        }

        // Deduct coins from the user's wallet (bidPrice coins per bid)
        int coinsRequired = product.getBidPrice().intValue();
        walletService.spendCoins(userId, coinsRequired);

        // Save the bid
        Bid bid = Bid.builder()
                .userId(userId)
                .productId(productId)
                .amount(product.getBidPrice())
                .placedAt(LocalDateTime.now())
                .build();

        Bid saved = bidRepository.save(bid);

        // Increment bidsCompleted on the product
        product.setBidsCompleted(product.getBidsCompleted() + 1);

        // Check if we've reached the required total — pick a winner if so
        if (product.getBidsCompleted() >= product.getTotalBidsRequired()) {
            selectWinner(product);
        } else {
            productRepository.save(product);
        }

        return toResponse(saved);
    }

    public List<BidResponse> getBidsByProduct(String productId) {
        return bidRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BidResponse> getBidsByUser(String userId) {
        return bidRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getWinner(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (product.getStatus() != Product.ProductStatus.COMPLETED) {
            throw new IllegalStateException("Winner not yet selected. Bidding is still open.");
        }

        return productService.toResponse(product);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void selectWinner(Product product) {
        List<Bid> bids = bidRepository.findByProductId(product.getId());

        if (bids.isEmpty()) {
            // Should never happen, but guard anyway
            productRepository.save(product);
            return;
        }

        Bid winningBid = bids.get(new Random().nextInt(bids.size()));

        product.setWinnerId(winningBid.getUserId());
        product.setStatus(Product.ProductStatus.COMPLETED);
        productRepository.save(product);
    }

    private BidResponse toResponse(Bid bid) {
        BidResponse response = new BidResponse();
        response.setId(bid.getId());
        response.setUserId(bid.getUserId());
        response.setProductId(bid.getProductId());
        response.setAmount(bid.getAmount());
        response.setPlacedAt(bid.getPlacedAt());
        return response;
    }
}
