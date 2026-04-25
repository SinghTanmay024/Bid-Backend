package com.bidbackend.controller;

import com.bidbackend.dto.CoinPurchaseRequest;
import com.bidbackend.dto.CoinSpendRequest;
import com.bidbackend.dto.WalletResponse;
import com.bidbackend.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users/{userId}/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Coin balance management")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get current coin balance for a user")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String userId) {
        return ResponseEntity.ok(walletService.getWallet(userId));
    }

    @PostMapping("/purchase")
    @Operation(summary = "Credit coins after a successful payment")
    public ResponseEntity<WalletResponse> purchaseCoins(
            @PathVariable String userId,
            @Valid @RequestBody CoinPurchaseRequest request) {
        return ResponseEntity.ok(walletService.purchaseCoins(userId, request.getCoins()));
    }

    @PutMapping("/spend")
    @Operation(summary = "Deduct coins when placing a bid")
    public ResponseEntity<WalletResponse> spendCoins(
            @PathVariable String userId,
            @Valid @RequestBody CoinSpendRequest request) {
        return ResponseEntity.ok(walletService.spendCoins(userId, request.getCoins()));
    }
}
