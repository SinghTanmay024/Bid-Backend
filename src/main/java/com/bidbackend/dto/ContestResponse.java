package com.bidbackend.dto;

import com.bidbackend.model.Contest;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ContestResponse {

    private String id;
    private String title;
    private String description;
    private String productDetails;
    private Double entryFee;
    private Integer maxParticipants;
    private Integer participantCount;
    private Instant startTime;
    private Instant endTime;
    private String imageUrl;
    private String status;
    private String winnerType;
    private List<Contest.ContestTier> tiers;
    private String createdBy;
    private String rejectionReason;
    private Instant drawnAt;
}
