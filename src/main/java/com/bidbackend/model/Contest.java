package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "contests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contest {

    @Id
    private String id;

    private String title;
    private String description;
    private String productDetails;

    @Builder.Default
    private Double entryFee = 0.0;

    @Builder.Default
    private Integer maxParticipants = 1000;

    @Builder.Default
    private Integer participantCount = 0;

    private Instant startTime;
    private Instant endTime;

    private String imageUrl;

    @Builder.Default
    private ContestStatus status = ContestStatus.PENDING;

    private String winnerType;   // "single" or "multiple"

    private List<ContestTier> tiers;

    private String createdBy;    // email of the creator

    /** Reason stored when admin rejects. */
    private String rejectionReason;

    /** Timestamp when the draw was executed (null until drawn). */
    private Instant drawnAt;

    /**
     * Full ordered shuffle list — stored permanently after draw.
     * Each entry is a userId (email). Index 0 = first winner slot.
     */
    private List<String> shuffleLog;

    /** SHA-256 seed used for the deterministic draw. */
    private String drawSeed;

    // -------------------------------------------------------------------------
    // Embedded types
    // -------------------------------------------------------------------------

    public enum ContestStatus {
        PENDING, UPCOMING, OPEN, COMPLETED, REJECTED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContestTier {
        private String rank;          // e.g. "1st"
        private Integer winnersCount;
        private String prize;
    }
}
