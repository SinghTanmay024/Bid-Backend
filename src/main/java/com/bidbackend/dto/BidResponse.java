package com.bidbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BidResponse {

    private String id;
    private String userId;
    private String productId;
    private Double amount;
    private LocalDateTime placedAt;
}
