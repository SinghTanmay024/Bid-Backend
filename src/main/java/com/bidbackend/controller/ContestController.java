package com.bidbackend.controller;

import com.bidbackend.dto.*;
import com.bidbackend.service.ContestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
@Tag(name = "Contests", description = "Contest lifecycle, entry, referral, draw and transparency")
public class ContestController {

    private final ContestService contestService;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    /** POST /api/contests — create a new contest (starts PENDING) */
    @PostMapping
    @Operation(summary = "Create a contest")
    public ResponseEntity<ContestResponse> createContest(
            @Valid @RequestBody ContestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(contestService.createContest(request, userDetails.getUsername()));
    }

    /** GET /api/contests — list all contests (public) */
    @GetMapping
    @Operation(summary = "List all contests")
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        return ResponseEntity.ok(contestService.getAllContests());
    }

    /**
     * GET /api/contests/transparency/all — all drawn contests summary (public).
     * IMPORTANT: must be declared before /{id} so Spring doesn't treat "transparency" as an id.
     */
    @GetMapping("/transparency/all")
    @Operation(summary = "All transparency records for drawn contests")
    public ResponseEntity<List<Map<String, Object>>> getAllTransparency() {
        return ResponseEntity.ok(contestService.getAllTransparency());
    }

    /** GET /api/contests/{id} — single contest (public) */
    @GetMapping("/{id}")
    @Operation(summary = "Get contest by ID")
    public ResponseEntity<ContestResponse> getContest(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getContestById(id));
    }

    /** PUT /api/contests/{id} — update contest (owner or admin) */
    @PutMapping("/{id}")
    @Operation(summary = "Update contest")
    public ResponseEntity<ContestResponse> updateContest(
            @PathVariable String id,
            @RequestBody ContestRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(
                contestService.updateContest(id, request, userDetails.getUsername(), isAdmin));
    }

    // -------------------------------------------------------------------------
    // Admin actions
    // -------------------------------------------------------------------------

    /** POST /api/contests/{id}/approve — admin only */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve contest (admin)")
    public ResponseEntity<Map<String, String>> approveContest(@PathVariable String id) {
        contestService.approveContest(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Contest approved"));
    }

    /** POST /api/contests/{id}/reject — admin only, body: { reason } */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject contest (admin)")
    public ResponseEntity<Map<String, String>> rejectContest(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        contestService.rejectContest(id, body.getOrDefault("reason", ""));
        return ResponseEntity.ok(Collections.singletonMap("message", "Contest rejected"));
    }

    /** POST /api/contests/{id}/draw — admin only */
    @PostMapping("/{id}/draw")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Execute the fairness draw (admin)")
    public ResponseEntity<DrawResponse> drawContest(@PathVariable String id) {
        return ResponseEntity.ok(contestService.draw(id));
    }

    // -------------------------------------------------------------------------
    // Entry
    // -------------------------------------------------------------------------

    /** POST /api/contests/{id}/enter — body: { userId, referredBy } */
    @PostMapping("/{id}/enter")
    @Operation(summary = "Enter a contest")
    public ResponseEntity<Map<String, Object>> enterContest(
            @PathVariable String id,
            @Valid @RequestBody ContestEntryRequest request,
            HttpServletRequest httpRequest) {
        String entryId = contestService.enterContest(id, request, httpRequest);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("message", "Entered successfully");
        response.put("entryId", entryId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Participants
    // -------------------------------------------------------------------------

    /** GET /api/contests/{id}/participants — public */
    @GetMapping("/{id}/participants")
    @Operation(summary = "List participants")
    public ResponseEntity<List<Map<String, Object>>> getParticipants(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getParticipants(id));
    }

    /** GET /api/contests/{id}/entry/{userId} — returns { entered: true/false } */
    @GetMapping("/{id}/entry/{userId}")
    @Operation(summary = "Check if user has entered contest")
    public ResponseEntity<Map<String, Boolean>> checkEntry(
            @PathVariable String id,
            @PathVariable String userId) {
        boolean entered = contestService.hasEntered(id, userId);
        return ResponseEntity.ok(Collections.singletonMap("entered", entered));
    }

    // -------------------------------------------------------------------------
    // Referral
    // -------------------------------------------------------------------------

    /** POST /api/contests/{id}/referral — body: { userId } → returns { referralLink } */
    @PostMapping("/{id}/referral")
    @Operation(summary = "Register referral and get link")
    public ResponseEntity<Map<String, String>> registerReferral(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String link = contestService.registerReferral(id, body.get("userId"));
        return ResponseEntity.ok(Collections.singletonMap("referralLink", link));
    }

    /** GET /api/contests/{id}/referral/{userId} — returns { referrals, bonusEntries } */
    @GetMapping("/{id}/referral/{userId}")
    @Operation(summary = "Get referral stats for a user in a contest")
    public ResponseEntity<Map<String, Object>> getReferralStats(
            @PathVariable String id,
            @PathVariable String userId) {
        return ResponseEntity.ok(contestService.getReferralStats(id, userId));
    }

    // -------------------------------------------------------------------------
    // Winners & Transparency
    // -------------------------------------------------------------------------

    /** GET /api/contests/{id}/winners — public */
    @GetMapping("/{id}/winners")
    @Operation(summary = "Get winners for a drawn contest")
    public ResponseEntity<List<DrawResponse.WinnerEntry>> getWinners(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getWinners(id));
    }

    /** GET /api/contests/{id}/draw-log — public */
    @GetMapping("/{id}/draw-log")
    @Operation(summary = "Get draw log / audit info")
    public ResponseEntity<DrawResponse.DrawLog> getDrawLog(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getDrawLog(id));
    }

    /** GET /api/contests/{id}/transparency — public */
    @GetMapping("/{id}/transparency")
    @Operation(summary = "Full transparency report for a drawn contest")
    public ResponseEntity<TransparencyResponse> getTransparency(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getTransparency(id));
    }
}
