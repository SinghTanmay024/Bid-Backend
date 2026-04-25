package com.bidbackend.dto;

import lombok.Data;

@Data
public class UserProfileResponse {

    private String id;
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
}
