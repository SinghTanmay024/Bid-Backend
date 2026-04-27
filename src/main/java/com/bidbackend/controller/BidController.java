package com.bidbackend.controller;

import com.bidbackend.dto.BidResponse;
import com.bidbackend.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
@Tag(name = "Bids", description = "Place and view bids")
public class BidController {

    private final BidService bidService;

    /**
     * POST /api/bids/{productId}?userId=<email>
     * Place a bid. Each user can bid only once per product.
     */
    @PostMapping("/{productId}")
    @Operation(summary = "Place a bid",
               description = "Place a bid on a product. Pass userId as a query param.")
    public ResponseEntity<Map<String, String>> placeBid(
            @PathVariable String productId,
            @RequestParam String userId) {
        bidService.placeBid(productId, userId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Bid placed successfully"));
    }

    /** GET /api/bids/product/{productId} — all bids for a product */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get bids for a product")
    public ResponseEntity<List<BidResponse>> getBidsByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(bidService.getBidsByProduct(productId));
    }

    /** GET /api/bids/user/{userId} — all bids by a user */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bids by user")
    public ResponseEntity<List<BidResponse>> getBidsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(bidService.getBidsByUser(userId));
    }

    /**
     * GET /api/bids/winner/{productId}
     * Returns { winnerId: "<email>" }. 404 if no winner yet.
     */
    @GetMapping("/winner/{productId}")
    @Operation(summary = "Get winner for a product")
    public ResponseEntity<Map<String, String>> getWinner(@PathVariable String productId) {
        String winnerId = bidService.getWinnerId(productId);
        return ResponseEntity.ok(Collections.singletonMap("winnerId", winnerId));
    }
}
