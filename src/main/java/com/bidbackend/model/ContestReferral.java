package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Tracks referrals per contest.
 * Each time a user enters via a referral link the referrer gets +1 bonus entry.
 */
@Document(collection = "contest_referrals")
@CompoundIndex(name = "contest_referrer_unique", def = "{'contestId': 1, 'referrerId': 1, 'referredUserId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestReferral {

    @Id
    private String id;

    private String contestId;

    private String referrerId;       // email of the user who shared the link

    private String referredUserId;   // email of the user who entered via the link

    @CreatedDate
    @Builder.Default
    private Instant createdAt = Instant.now();
}
