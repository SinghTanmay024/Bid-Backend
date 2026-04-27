package com.bidbackend.service;

import com.bidbackend.dto.*;
import com.bidbackend.model.*;
import com.bidbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContestService {

    private static final String ALGORITHM = "Mulberry32 PRNG";

    private final ContestRepository contestRepository;
    private final ContestParticipantRepository participantRepository;
    private final ContestWinnerRepository winnerRepository;
    private final ContestReferralRepository referralRepository;

    // =========================================================================
    // Create
    // =========================================================================

    public ContestResponse createContest(ContestRequest request, String creatorEmail) {
        List<Contest.ContestTier> tiers = null;
        if (request.getTiers() != null) {
            tiers = request.getTiers().stream()
                    .map(t -> Contest.ContestTier.builder()
                            .rank(t.getRank())
                            .winnersCount(t.getWinnersCount())
                            .prize(t.getPrize())
                            .build())
                    .collect(Collectors.toList());
        }

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .productDetails(request.getProductDetails())
                .entryFee(request.getEntryFee())
                .maxParticipants(request.getMaxParticipants())
                .startTime(parseFlexible(request.getStartTime()))
                .endTime(parseFlexible(request.getEndTime()))
                .imageUrl(request.getImageUrl())
                .status(Contest.ContestStatus.PENDING)
                .winnerType(request.getWinnerType())
                .tiers(tiers)
                .createdBy(creatorEmail)
                .build();

        Contest saved = contestRepository.save(contest);
        return toResponse(saved);
    }

    // =========================================================================
    // List / Get
    // =========================================================================

    public List<ContestResponse> getAllContests() {
        return contestRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ContestResponse getContestById(String id) {
        return toResponse(findContest(id));
    }

    // =========================================================================
    // Update
    // =========================================================================

    public ContestResponse updateContest(String id, ContestRequest request, String requestorEmail, boolean isAdmin) {
        Contest contest = findContest(id);

        if (!isAdmin && !contest.getCreatedBy().equals(requestorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to update this contest.");
        }

        if (request.getTitle() != null)          contest.setTitle(request.getTitle());
        if (request.getDescription() != null)    contest.setDescription(request.getDescription());
        if (request.getProductDetails() != null) contest.setProductDetails(request.getProductDetails());
        if (request.getEntryFee() != null)       contest.setEntryFee(request.getEntryFee());
        if (request.getMaxParticipants() != null) contest.setMaxParticipants(request.getMaxParticipants());
        if (request.getStartTime() != null)      contest.setStartTime(parseFlexible(request.getStartTime()));
        if (request.getEndTime() != null)        contest.setEndTime(parseFlexible(request.getEndTime()));
        if (request.getImageUrl() != null)       contest.setImageUrl(request.getImageUrl());
        if (request.getWinnerType() != null)     contest.setWinnerType(request.getWinnerType());
        if (request.getTiers() != null) {
            contest.setTiers(request.getTiers().stream()
                    .map(t -> Contest.ContestTier.builder()
                            .rank(t.getRank())
                            .winnersCount(t.getWinnersCount())
                            .prize(t.getPrize())
                            .build())
                    .collect(Collectors.toList()));
        }

        return toResponse(contestRepository.save(contest));
    }

    // =========================================================================
    // Admin: approve / reject
    // =========================================================================

    public void approveContest(String id) {
        Contest contest = findContest(id);
        contest.setStatus(Contest.ContestStatus.UPCOMING);
        contestRepository.save(contest);
    }

    public void rejectContest(String id, String reason) {
        Contest contest = findContest(id);
        contest.setStatus(Contest.ContestStatus.REJECTED);
        contest.setRejectionReason(reason);
        contestRepository.save(contest);
    }

    // =========================================================================
    // Enter
    // =========================================================================

    public String enterContest(String id, ContestEntryRequest request, HttpServletRequest httpRequest) {
        Contest contest = findContest(id);

        if (contest.getStatus() != Contest.ContestStatus.OPEN) {
            throw new IllegalStateException("Contest is not open for entry.");
        }

        if (contest.getParticipantCount() >= contest.getMaxParticipants()) {
            throw new IllegalStateException("Contest is full.");
        }

        if (participantRepository.existsByContestIdAndUserId(id, request.getUserId())) {
            throw new IllegalStateException("You have already entered this contest.");
        }

        String ip = resolveIp(httpRequest);

        ContestParticipant participant = ContestParticipant.builder()
                .contestId(id)
                .userId(request.getUserId())
                .referredBy(request.getReferredBy())
                .ipAddress(ip)
                .build();

        ContestParticipant saved;
        try {
            saved = participantRepository.save(participant);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalStateException("You have already entered this contest.");
        }

        // Grant bonus entry to referrer
        if (request.getReferredBy() != null && !request.getReferredBy().trim().isEmpty()) {
            if (participantRepository.existsByContestIdAndUserId(id, request.getReferredBy())) {
                participantRepository.findByContestIdAndUserId(id, request.getReferredBy())
                        .ifPresent(referrer -> {
                            referrer.setBonusEntries(referrer.getBonusEntries() + 1);
                            participantRepository.save(referrer);
                        });
            }
            // Record the referral
            if (!referralRepository.existsByContestIdAndReferrerIdAndReferredUserId(
                    id, request.getReferredBy(), request.getUserId())) {
                referralRepository.save(ContestReferral.builder()
                        .contestId(id)
                        .referrerId(request.getReferredBy())
                        .referredUserId(request.getUserId())
                        .build());
            }
        }

        contest.setParticipantCount(contest.getParticipantCount() + 1);
        contestRepository.save(contest);

        return saved.getId();
    }

    // =========================================================================
    // Participants
    // =========================================================================

    public List<Map<String, Object>> getParticipants(String id) {
        return participantRepository.findByContestId(id).stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", p.getUserId());
                    m.put("enteredAt", p.getEnteredAt());
                    m.put("bonusEntries", p.getBonusEntries());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public boolean hasEntered(String id, String userId) {
        return participantRepository.existsByContestIdAndUserId(id, userId);
    }

    // =========================================================================
    // Referral
    // =========================================================================

    public String registerReferral(String id, String userId) {
        findContest(id); // validate contest exists
        return "https://bidwin.app/contests/" + id + "?ref=" + userId;
    }

    public Map<String, Object> getReferralStats(String id, String userId) {
        long referrals = referralRepository.countByContestIdAndReferrerId(id, userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("referrals", referrals);
        result.put("bonusEntries", referrals);
        return result;
    }

    // =========================================================================
    // Draw (Fairness Engine — Mulberry32 PRNG + Fisher-Yates)
    // =========================================================================

    public DrawResponse draw(String id) {
        Contest contest = findContest(id);

        // Only draw once
        if (winnerRepository.existsByContestId(id)) {
            throw new IllegalStateException("This contest has already been drawn.");
        }

        // Snapshot participant list
        List<ContestParticipant> participants = participantRepository.findByContestId(id);
        if (participants.isEmpty()) {
            throw new IllegalStateException("No participants to draw from.");
        }

        // Build seed: "<contestId>::<endTime ISO>::<totalParticipants>"
        String endTimeIso = contest.getEndTime().toString();
        String seedString = id + "::" + endTimeIso + "::" + participants.size();

        // SHA-256 the seed string
        String hexSeed = sha256Hex(seedString);

        // Convert first 8 hex chars to a 32-bit unsigned int for Mulberry32
        long seed32 = Long.parseLong(hexSeed.substring(0, 8), 16);

        // Extract user ids in deterministic order (sort by enteredAt, then userId as tiebreak)
        List<String> userIds = participants.stream()
                .sorted(Comparator.comparing(ContestParticipant::getEnteredAt)
                        .thenComparing(ContestParticipant::getUserId))
                .map(ContestParticipant::getUserId)
                .collect(Collectors.toList());

        // Fisher-Yates shuffle using Mulberry32 PRNG
        Mulberry32 rng = new Mulberry32(seed32);
        for (int i = userIds.size() - 1; i > 0; i--) {
            int j = (int) (rng.next() * (i + 1));
            Collections.swap(userIds, i, j);
        }

        // Assign winners in tier order
        Instant drawnAt = Instant.now();
        List<DrawResponse.WinnerEntry> winnerEntries = new ArrayList<>();
        List<ContestWinner> winnerDocs = new ArrayList<>();

        int cursor = 0;
        List<Contest.ContestTier> tiers = contest.getTiers() != null ? contest.getTiers() : Collections.emptyList();

        for (Contest.ContestTier tier : tiers) {
            int count = Math.min(tier.getWinnersCount(), userIds.size() - cursor);
            for (int i = 0; i < count; i++) {
                String winnerUserId = userIds.get(cursor++);

                DrawResponse.WinnerEntry entry = new DrawResponse.WinnerEntry();
                entry.setUserId(winnerUserId);
                entry.setTier(tier);
                winnerEntries.add(entry);

                winnerDocs.add(ContestWinner.builder()
                        .contestId(id)
                        .userId(winnerUserId)
                        .tier(tier)
                        .build());
            }
            if (cursor >= userIds.size()) break;
        }

        // Persist winners (immutable)
        winnerRepository.saveAll(winnerDocs);

        // Persist draw metadata on contest
        contest.setDrawnAt(drawnAt);
        contest.setDrawSeed(hexSeed);
        contest.setShuffleLog(new ArrayList<>(userIds));
        contestRepository.save(contest);

        // Build response
        DrawResponse.DrawLog log = new DrawResponse.DrawLog();
        log.setSeed(hexSeed);
        log.setAlgorithm(ALGORITHM);
        log.setTotalParticipants(participants.size());
        log.setDrawnAt(drawnAt);

        DrawResponse response = new DrawResponse();
        response.setWinners(winnerEntries);
        response.setLog(log);
        return response;
    }

    // =========================================================================
    // Winners + Transparency
    // =========================================================================

    public List<DrawResponse.WinnerEntry> getWinners(String id) {
        List<ContestWinner> winners = winnerRepository.findByContestId(id);
        return winners.stream()
                .map(w -> {
                    DrawResponse.WinnerEntry e = new DrawResponse.WinnerEntry();
                    e.setUserId(w.getUserId());
                    e.setTier(w.getTier());
                    return e;
                })
                .collect(Collectors.toList());
    }

    public DrawResponse.DrawLog getDrawLog(String id) {
        Contest contest = findContest(id);
        if (contest.getDrawnAt() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest has not been drawn yet.");
        }
        List<ContestParticipant> participants = participantRepository.findByContestId(id);

        DrawResponse.DrawLog log = new DrawResponse.DrawLog();
        log.setSeed(contest.getDrawSeed());
        log.setAlgorithm(ALGORITHM);
        log.setTotalParticipants(participants.size());
        log.setDrawnAt(contest.getDrawnAt());
        return log;
    }

    public TransparencyResponse getTransparency(String id) {
        Contest contest = findContest(id);
        if (contest.getDrawnAt() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest has not been drawn yet.");
        }

        List<ContestWinner> winners = winnerRepository.findByContestId(id);

        // Group winners by tier rank
        Map<String, List<String>> winnersByRank = new LinkedHashMap<>();
        Map<String, String> prizeByRank = new LinkedHashMap<>();
        for (ContestWinner w : winners) {
            String rank = w.getTier().getRank();
            winnersByRank.computeIfAbsent(rank, k -> new ArrayList<>()).add(w.getUserId());
            prizeByRank.put(rank, w.getTier().getPrize());
        }

        List<TransparencyResponse.TierResult> tierResults = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : winnersByRank.entrySet()) {
            TransparencyResponse.TierResult tr = new TransparencyResponse.TierResult();
            tr.setRank(entry.getKey());
            tr.setPrize(prizeByRank.get(entry.getKey()));
            tr.setWinners(entry.getValue());
            tierResults.add(tr);
        }

        List<ContestParticipant> participants = participantRepository.findByContestId(id);

        TransparencyResponse response = new TransparencyResponse();
        response.setContestId(id);
        response.setEndTime(contest.getEndTime());
        response.setTotalParticipants(participants.size());
        response.setTotalWinners(winners.size());
        response.setMethod("Mulberry32 PRNG + Fisher-Yates Shuffle");
        response.setDrawnAt(contest.getDrawnAt());
        response.setTierResults(tierResults);
        response.setShuffleLog(contest.getShuffleLog() != null ? contest.getShuffleLog() : Collections.emptyList());
        return response;
    }

    public List<Map<String, Object>> getAllTransparency() {
        return contestRepository.findByDrawnAtIsNotNullOrderByDrawnAtDesc().stream()
                .map(c -> {
                    List<ContestWinner> winners = winnerRepository.findByContestId(c.getId());
                    List<ContestParticipant> participants = participantRepository.findByContestId(c.getId());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("contestId", c.getId());
                    m.put("contestTitle", c.getTitle());
                    m.put("totalParticipants", participants.size());
                    m.put("totalWinners", winners.size());
                    m.put("drawnAt", c.getDrawnAt());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Scheduler helpers (called by ContestScheduler)
    // =========================================================================

    public void openDueContests() {
        contestRepository.findByStatusAndStartTimeBefore(Contest.ContestStatus.UPCOMING, Instant.now())
                .forEach(c -> {
                    c.setStatus(Contest.ContestStatus.OPEN);
                    contestRepository.save(c);
                });
    }

    public void closeDueContests() {
        contestRepository.findByStatusAndEndTimeBefore(Contest.ContestStatus.OPEN, Instant.now())
                .forEach(c -> {
                    c.setStatus(Contest.ContestStatus.COMPLETED);
                    contestRepository.save(c);
                });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Parses flexible datetime strings into Instant.
     * Handles: "2026-04-01T17:18"          (no seconds, no zone  → treated as UTC)
     *          "2026-04-01T17:18:00"        (with seconds, no zone → UTC)
     *          "2026-04-01T17:18:00Z"        (UTC)
     *          "2026-04-01T17:18:00+05:30"   (with offset)
     *          "2026-04-01 17:18"            (space separator)
     */
    static Instant parseFlexible(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String s = raw.trim().replace(' ', 'T');

        // Already has offset/Z — let Instant.parse handle it directly
        if (s.endsWith("Z") || s.contains("+") || (s.length() > 19 && s.charAt(19) == '-')) {
            try { return Instant.parse(s); } catch (DateTimeParseException ignored) { /* fall through */ }
        }

        // No offset: parse as LocalDateTime then treat as UTC
        DateTimeFormatterBuilder fb = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm")
                .optionalStart().appendPattern(":ss").optionalEnd()
                .optionalStart().appendPattern(".SSS").optionalEnd()
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
        try {
            return LocalDateTime.parse(s, fb.toFormatter()).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date-time: '" + raw + "'. Use e.g. 2026-04-01T17:18 or 2026-04-01T17:18:00Z");
        }
    }

    private Contest findContest(String id) {
        return contestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contest not found."));
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // =========================================================================
    // Mulberry32 PRNG — deterministic 32-bit pseudo-random number generator
    // =========================================================================

    private static class Mulberry32 {
        private long state;   // kept as long to simulate unsigned 32-bit arithmetic

        Mulberry32(long seed) {
            this.state = seed & 0xFFFFFFFFL;
        }

        /**
         * Returns the next pseudo-random float in [0, 1).
         * Algorithm: https://gist.github.com/tommyettinger/46a874533244883189143505d203312c
         */
        double next() {
            state = (state + 0x6D2B79F5L) & 0xFFFFFFFFL;
            long z = state;
            z = ((z ^ (z >>> 15)) * (z | 1L)) & 0xFFFFFFFFL;
            z ^= z + ((z ^ (z >>> 7)) * (z | 61L)) & 0xFFFFFFFFL;
            z = (z ^ (z >>> 14)) & 0xFFFFFFFFL;
            // Map to [0, 1)
            return (z & 0xFFFFFFFFL) / 4294967296.0;
        }
    }

    // =========================================================================
    // DTO mapping
    // =========================================================================

    public ContestResponse toResponse(Contest c) {
        ContestResponse r = new ContestResponse();
        r.setId(c.getId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setProductDetails(c.getProductDetails());
        r.setEntryFee(c.getEntryFee());
        r.setMaxParticipants(c.getMaxParticipants());
        r.setParticipantCount(c.getParticipantCount());
        r.setStartTime(c.getStartTime());
        r.setEndTime(c.getEndTime());
        r.setImageUrl(c.getImageUrl());
        r.setStatus(c.getStatus() != null ? c.getStatus().name() : null);
        r.setWinnerType(c.getWinnerType());
        r.setTiers(c.getTiers());
        r.setCreatedBy(c.getCreatedBy());
        r.setRejectionReason(c.getRejectionReason());
        r.setDrawnAt(c.getDrawnAt());
        return r;
    }
}
