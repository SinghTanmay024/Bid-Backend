package com.bidbackend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String description;

    private Double bidPrice;            // price per bid (e.g. 20 rs)

    private Integer totalBidsRequired;  // total bids needed to trigger winner selection

    private Integer bidsCompleted;      // how many bids have been placed so far

    private String imageUrl;

    private ProductStatus status;       // OPEN or COMPLETED

    private String winnerId;            // userId of the winner (set after completion)

    public enum ProductStatus {
        OPEN, COMPLETED
    }
}
