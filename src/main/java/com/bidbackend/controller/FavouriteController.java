package com.bidbackend.controller;

import com.bidbackend.service.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
@Tag(name = "Favourites", description = "Save and retrieve favourite products")
public class FavouriteController {

    private final FavouriteService favouriteService;

    /** GET /api/favourites — returns list of product ID strings */
    @GetMapping
    @Operation(summary = "Get favourites", description = "Returns list of productId strings for the logged-in user")
    public ResponseEntity<List<String>> getFavourites(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favouriteService.getFavourites(userDetails.getUsername()));
    }

    /** POST /api/favourites/{productId} — idempotent add */
    @PostMapping("/{productId}")
    @Operation(summary = "Add to favourites", description = "Add a product to favourites (idempotent)")
    public ResponseEntity<Map<String, String>> addFavourite(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favouriteService.addFavourite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Added to favourites"));
    }

    /** DELETE /api/favourites/{productId} */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove from favourites")
    public ResponseEntity<Map<String, String>> removeFavourite(
            @PathVariable String productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favouriteService.removeFavourite(userDetails.getUsername(), productId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Removed from favourites"));
    }
}
