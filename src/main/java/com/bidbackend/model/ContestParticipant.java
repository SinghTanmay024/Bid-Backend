package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * One record per (contest, user) entry.
 * The compound unique index prevents duplicate entries even under concurrent requests.
 */
@Document(collection = "contest_participants")
@CompoundIndex(name = "contest_user_unique", def = "{'contestId': 1, 'userId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestParticipant {

    @Id
    private String id;

    private String contestId;

    private String userId;         // email

    @Builder.Default
    private Instant enteredAt = Instant.now();

    @Builder.Default
    private Integer bonusEntries = 0;

    private String referredBy;     // email of the referrer (nullable)

    /** IP address at entry time — used for fraud detection. */
    private String ipAddress;
}
