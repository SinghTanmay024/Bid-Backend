package com.bidbackend.service;

import com.bidbackend.dto.AdminStatsResponse;
import com.bidbackend.dto.FraudAlertResponse;
import com.bidbackend.model.Contest;
import com.bidbackend.model.ContestParticipant;
import com.bidbackend.model.User;
import com.bidbackend.repository.ContestParticipantRepository;
import com.bidbackend.repository.ContestRepository;
import com.bidbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int HIGH_REFERRAL_THRESHOLD = 20;
    private static final int NEW_ACCOUNT_MINUTES = 10;
    private static final long REFERRAL_WINDOW_MINUTES = 60;

    private final UserRepository userRepository;
    private final ContestRepository contestRepository;
    private final ContestParticipantRepository participantRepository;

    // =========================================================================
    // Stats
    // =========================================================================

    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long activeContests = contestRepository.findByStatus(Contest.ContestStatus.OPEN).size();

        double totalRevenue = contestRepository.findAll().stream()
                .filter(c -> c.getEntryFee() != null && c.getEntryFee() > 0)
                .mapToDouble(c -> c.getEntryFee() * c.getParticipantCount())
                .sum();

        return new AdminStatsResponse(totalUsers, activeContests, totalRevenue);
    }

    // =========================================================================
    // Fraud detection heuristics
    // =========================================================================

    public List<FraudAlertResponse> getFraudAlerts() {
        Map<String, FraudAlertResponse> alerts = new LinkedHashMap<>();

        // Heuristic 1: Multiple entries from the same IP in a single contest
        contestRepository.findAll().forEach(contest -> {
            List<ContestParticipant> participants = participantRepository.findByContestId(contest.getId());
            Map<String, List<ContestParticipant>> byIp = participants.stream()
                    .filter(p -> p.getIpAddress() != null)
                    .collect(Collectors.groupingBy(ContestParticipant::getIpAddress));

            byIp.forEach((ip, entries) -> {
                if (entries.size() > 1) {
                    entries.forEach(p -> {
                        String key = p.getUserId() + ":ip_dup";
                        FraudAlertResponse alert = alerts.computeIfAbsent(key,
                                k -> new FraudAlertResponse(p.getUserId(),
                                        "Multiple entries from same IP (" + ip + ") in contest " + contest.getId(), 0));
                        alert.setCount(alert.getCount() + 1);
                    });
                }
            });
        });

        // Heuristic 2: Unusually high referral count in < 1 hour
        Instant oneHourAgo = Instant.now().minus(REFERRAL_WINDOW_MINUTES, ChronoUnit.MINUTES);
        List<ContestParticipant> allParticipants = participantRepository.findAll();
        Map<String, Long> referralCounts = allParticipants.stream()
                .filter(p -> p.getReferredBy() != null && p.getEnteredAt().isAfter(oneHourAgo))
                .collect(Collectors.groupingBy(ContestParticipant::getReferredBy, Collectors.counting()));

        referralCounts.forEach((referrerId, count) -> {
            if (count > HIGH_REFERRAL_THRESHOLD) {
                String key = referrerId + ":high_referral";
                alerts.put(key, new FraudAlertResponse(
                        referrerId,
                        "Unusually high referral count (" + count + ") in the last hour",
                        count.intValue()));
            }
        });

        // Heuristic 3: Accounts created < 10 minutes before entering a contest
        List<User> recentUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null &&
                        u.getCreatedAt().isAfter(Instant.now().minus(NEW_ACCOUNT_MINUTES * 2L, ChronoUnit.MINUTES)))
                .collect(Collectors.toList());

        recentUsers.forEach(user -> {
            Instant cutoff = user.getCreatedAt().plus(NEW_ACCOUNT_MINUTES, ChronoUnit.MINUTES);
            List<ContestParticipant> earlyEntries = participantRepository
                    .findByUserIdAndEnteredAtAfter(user.getEmail(), user.getCreatedAt()).stream()
                    .filter(p -> p.getEnteredAt().isBefore(cutoff))
                    .collect(Collectors.toList());

            if (!earlyEntries.isEmpty()) {
                String key = user.getEmail() + ":new_account";
                alerts.put(key, new FraudAlertResponse(
                        user.getEmail(),
                        "Account entered contest within " + NEW_ACCOUNT_MINUTES + " minutes of creation",
                        earlyEntries.size()));
            }
        });

        return new ArrayList<>(alerts.values());
    }

    // =========================================================================
    // Flag / block user
    // =========================================================================

    public void flagUser(String userId, String reason) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        user.setBlocked(true);
        userRepository.save(user);
    }
}
