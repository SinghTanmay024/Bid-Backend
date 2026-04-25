package com.bidbackend.service;

import com.bidbackend.dto.UserProfileRequest;
import com.bidbackend.dto.UserProfileResponse;
import com.bidbackend.model.UserProfile;
import com.bidbackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResponse createProfile(UserProfileRequest request) {
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Profile already exists for email: " + request.getEmail());
        }

        UserProfile profile = UserProfile.builder()
                .userId(request.getUserId())
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .build();

        UserProfile saved = userProfileRepository.save(profile);
        return toResponse(saved);
    }

    public UserProfileResponse getProfileByUserId(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));
        return toResponse(profile);
    }

    public UserProfileResponse updateProfile(String userId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        profile.setUsername(request.getUsername());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddressLine1(request.getAddressLine1());
        profile.setAddressLine2(request.getAddressLine2());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setPincode(request.getPincode());
        profile.setCountry(request.getCountry());

        UserProfile updated = userProfileRepository.save(profile);
        return toResponse(updated);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUserId());
        response.setUsername(profile.getUsername());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setEmail(profile.getEmail());
        response.setPhoneNumber(profile.getPhoneNumber());
        response.setAddressLine1(profile.getAddressLine1());
        response.setAddressLine2(profile.getAddressLine2());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setPincode(profile.getPincode());
        response.setCountry(profile.getCountry());
        return response;
    }
}
