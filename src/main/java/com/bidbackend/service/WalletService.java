package com.bidbackend.service;

import com.bidbackend.dto.WalletResponse;
import com.bidbackend.model.Wallet;
import com.bidbackend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    /** Returns the wallet for a user, creating it with 0 balance if it doesn't exist. */
    public WalletResponse getWallet(String userId) {
        Wallet wallet = getOrCreate(userId);
        return new WalletResponse(wallet.getUserId(), wallet.getCoinBalance());
    }

    /** Credit coins — called after a verified Razorpay payment. */
    public WalletResponse purchaseCoins(String userId, int coins) {
        Wallet wallet = getOrCreate(userId);
        wallet.setCoinBalance(wallet.getCoinBalance() + coins);
        walletRepository.save(wallet);
        return new WalletResponse(wallet.getUserId(), wallet.getCoinBalance());
    }

    /** Deduct coins — called when a bid is placed. Throws if balance is insufficient. */
    public WalletResponse spendCoins(String userId, int coins) {
        Wallet wallet = getOrCreate(userId);
        if (wallet.getCoinBalance() < coins) {
            throw new IllegalStateException(
                    "Insufficient coins. Balance: " + wallet.getCoinBalance() + ", required: " + coins);
        }
        wallet.setCoinBalance(wallet.getCoinBalance() - coins);
        walletRepository.save(wallet);
        return new WalletResponse(wallet.getUserId(), wallet.getCoinBalance());
    }

    // -------------------------------------------------------------------------
    // Package-private helper used by BidService
    // -------------------------------------------------------------------------

    Wallet getOrCreate(String userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet w = Wallet.builder().userId(userId).coinBalance(0).build();
            return walletRepository.save(w);
        });
    }
}
