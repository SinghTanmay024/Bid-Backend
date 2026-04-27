package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Immutable winner record — created once during draw, never edited.
 */
@Document(collection = "contest_winners")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestWinner {

    @Id
    private String id;

    private String contestId;

    private String userId;         // winner email

    private Contest.ContestTier tier;
}
