package com.bidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsResponse {

    private Long totalUsers;
    private Long activeContests;
    private Double totalRevenue;
}
