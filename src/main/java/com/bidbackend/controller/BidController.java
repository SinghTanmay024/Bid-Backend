package com.bidbackend.controller;

import com.bidbackend.dto.BidResponse;
import com.bidbackend.dto.ProductResponse;
import com.bidbackend.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
@Tag(name = "Bids", description = "Place and view bids")
public class BidController {

    private final BidService bidService;

    @PostMapping("/{productId}")
    @Operation(
            summary = "Place a bid",
            description = "Place a bid on a product. Pass userId as a query param. Each user can bid only once per product.")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable String productId,
            @RequestParam String userId) {
        return ResponseEntity.ok(bidService.placeBid(productId, userId));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get bids for a product", description = "Returns all bids placed on a given product")
    public ResponseEntity<List<BidResponse>> getBidsByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(bidService.getBidsByProduct(productId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bids by user", description = "Returns all bids placed by a given user")
    public ResponseEntity<List<BidResponse>> getBidsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(bidService.getBidsByUser(userId));
    }

    @GetMapping("/winner/{productId}")
    @Operation(summary = "Get winner", description = "Returns the product with the winner's userId once bidding is complete")
    public ResponseEntity<ProductResponse> getWinner(@PathVariable String productId) {
        return ResponseEntity.ok(bidService.getWinner(productId));
    }
}
