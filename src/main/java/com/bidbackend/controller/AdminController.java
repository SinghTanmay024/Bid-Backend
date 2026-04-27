package com.bidbackend.controller;

import com.bidbackend.dto.AdminStatsResponse;
import com.bidbackend.dto.FraudAlertResponse;
import com.bidbackend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints for platform stats and moderation")
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/stats
     * Returns totalUsers, activeContests, totalRevenue.
     */
    @GetMapping("/stats")
    @Operation(summary = "Platform statistics")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    /**
     * GET /api/admin/fraud-alerts
     * Returns list of users with suspicious behaviour.
     */
    @GetMapping("/fraud-alerts")
    @Operation(summary = "Fraud / suspicious activity alerts")
    public ResponseEntity<List<FraudAlertResponse>> getFraudAlerts() {
        return ResponseEntity.ok(adminService.getFraudAlerts());
    }

    /**
     * POST /api/admin/flag-user
     * Body: { userId, reason }
     * Marks the user as blocked — they cannot place bids or enter contests.
     */
    @PostMapping("/flag-user")
    @Operation(summary = "Flag and block a user")
    public ResponseEntity<Map<String, String>> flagUser(@RequestBody Map<String, String> body) {
        adminService.flagUser(body.get("userId"), body.get("reason"));
        return ResponseEntity.ok(Collections.singletonMap("message", "User flagged and blocked."));
    }
}
