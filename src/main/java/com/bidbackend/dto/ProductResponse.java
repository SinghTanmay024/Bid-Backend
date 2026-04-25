package com.bidbackend.dto;

import com.bidbackend.model.Product;
import lombok.Data;

@Data
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private Double bidPrice;
    private Integer totalBidsRequired;
    private Integer bidsCompleted;
    private String imageUrl;
    private Product.ProductStatus status;
    private String winnerId;
}
