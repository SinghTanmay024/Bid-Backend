package com.bidbackend.service;

import com.bidbackend.dto.BidResponse;
import com.bidbackend.model.Bid;
import com.bidbackend.model.Product;
import com.bidbackend.repository.BidRepository;
import com.bidbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final WalletService walletService;

    // -------------------------------------------------------------------------
    // Place bid
    // -------------------------------------------------------------------------

    public void placeBid(String productId, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));

        if (product.getStatus() == Product.ProductStatus.COMPLETED) {
            throw new IllegalStateException("Bidding is closed for this product.");
        }

        if (bidRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("You have already placed a bid on this product.");
        }

        // Deduct coins from the user's wallet
        int coinsRequired = product.getBidPrice().intValue();
        walletService.spendCoins(userId, coinsRequired);

        Bid bid = Bid.builder()
                .userId(userId)
                .productId(productId)
                .amount(product.getBidPrice())
                .placedAt(LocalDateTime.now())
                .build();

        bidRepository.save(bid);

        product.setBidsCompleted(product.getBidsCompleted() + 1);

        if (product.getBidsCompleted() >= product.getTotalBidsRequired()) {
            selectWinner(product);
        } else {
            productRepository.save(product);
        }
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

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

    /**
     * Returns the winnerId (email) for a completed product.
     * 404 if product not found or bidding is still open.
     */
    public String getWinnerId(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));

        if (product.getStatus() != Product.ProductStatus.COMPLETED || product.getWinnerId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No winner yet.");
        }

        return product.getWinnerId();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void selectWinner(Product product) {
        List<Bid> bids = bidRepository.findByProductId(product.getId());
        if (bids.isEmpty()) {
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
