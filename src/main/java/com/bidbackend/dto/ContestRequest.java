package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ContestRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String productDetails;

    @NotNull(message = "Entry fee is required")
    private Double entryFee;

    @NotNull(message = "Max participants is required")
    private Integer maxParticipants;

    /**
     * Accepts any reasonable date-time string the frontend sends:
     *   "2026-04-01T17:18"         (local, no seconds, no Z)
     *   "2026-04-01T17:18:00"      (local, with seconds)
     *   "2026-04-01T17:18:00Z"     (UTC ISO-8601)
     *   "2026-04-01T17:18:00+05:30" (with offset)
     * Parsed to Instant in ContestService via parseFlexible().
     */
    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End time is required")
    private String endTime;

    private String imageUrl;

    @NotBlank(message = "Winner type is required")
    private String winnerType;   // "single" or "multiple"

    private List<TierDto> tiers;

    @Data
    public static class TierDto {
        private String rank;
        private Integer winnersCount;
        private String prize;
    }
}
