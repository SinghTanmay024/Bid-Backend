package com.bidbackend.dto;

import com.bidbackend.model.Contest;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DrawResponse {

    private List<WinnerEntry> winners;
    private DrawLog log;

    @Data
    public static class WinnerEntry {
        private String userId;
        private Contest.ContestTier tier;
    }

    @Data
    public static class DrawLog {
        private String seed;
        private String algorithm;
        private Integer totalParticipants;
        private Instant drawnAt;
    }
}
