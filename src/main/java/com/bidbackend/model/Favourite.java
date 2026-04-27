package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "favourites")
@CompoundIndex(name = "user_product_unique", def = "{'userEmail': 1, 'productId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favourite {

    @Id
    private String id;

    private String userEmail;

    private String productId;

    @CreatedDate
    private Instant createdAt;
}
