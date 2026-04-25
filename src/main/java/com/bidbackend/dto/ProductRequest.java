package com.bidbackend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Bid price is required")
    @Min(value = 1, message = "Bid price must be at least 1")
    private Double bidPrice;

    @NotNull(message = "Total bids required is required")
    @Min(value = 1, message = "Total bids required must be at least 1")
    private Integer totalBidsRequired;

    private String imageUrl;
}
