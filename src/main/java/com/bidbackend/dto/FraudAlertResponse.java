package com.bidbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FraudAlertResponse {

    private String userId;   // email
    private String reason;
    private Integer count;
}
