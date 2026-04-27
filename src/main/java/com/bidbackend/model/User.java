package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String role;

    /** If true the account is suspended — all protected actions return 403. */
    @Builder.Default
    private boolean blocked = false;

    /** Timestamp used for fraud detection (account age check). */
    @Builder.Default
    private Instant createdAt = Instant.now();
}
