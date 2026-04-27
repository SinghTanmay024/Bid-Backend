package com.bidbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for auto-transitioning contest status:
 *   UPCOMING → OPEN  (when startTime is reached)
 *   OPEN     → COMPLETED (when endTime is reached)
 *
 * Runs every 60 seconds. Enable with @EnableScheduling on BidBackendApplication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContestScheduler {

    private final ContestService contestService;

    /** Every 60 s: open contests whose startTime has passed. */
    @Scheduled(fixedDelay = 60_000)
    public void openDueContests() {
        try {
            contestService.openDueContests();
        } catch (Exception e) {
            log.error("Error opening due contests: {}", e.getMessage());
        }
    }

    /** Every 60 s: close contests whose endTime has passed. */
    @Scheduled(fixedDelay = 60_000)
    public void closeDueContests() {
        try {
            contestService.closeDueContests();
        } catch (Exception e) {
            log.error("Error closing due contests: {}", e.getMessage());
        }
    }
}
