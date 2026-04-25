package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "bids")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id
    private String id;

    private String userId;

    private String productId;

    private Double amount;              // bid price paid

    private LocalDateTime placedAt;
}
