package com.bidbackend.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class TransparencyResponse {

    private String contestId;
    private Instant endTime;
    private Integer totalParticipants;
    private Integer totalWinners;
    private String method;
    private Instant drawnAt;
    private List<TierResult> tierResults;
    private List<String> shuffleLog;

    @Data
    public static class TierResult {
        private String rank;
        private String prize;
        private List<String> winners;  // list of email strings
    }
}
