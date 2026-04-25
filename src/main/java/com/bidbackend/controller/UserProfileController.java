package com.bidbackend.controller;

import com.bidbackend.dto.UserProfileRequest;
import com.bidbackend.dto.UserProfileResponse;
import com.bidbackend.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage user profile details")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/profile")
    @Operation(summary = "Create user profile", description = "Creates a new user profile with personal and address details")
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.createProfile(request));
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile", description = "Fetch profile details by userId")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userId));
    }

    @PutMapping("/profile/{userId}")
    @Operation(summary = "Update user profile", description = "Update an existing user profile by userId")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }
}
