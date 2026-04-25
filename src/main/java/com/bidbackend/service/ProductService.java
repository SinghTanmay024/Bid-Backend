package com.bidbackend.service;

import com.bidbackend.dto.ProductRequest;
import com.bidbackend.dto.ProductResponse;
import com.bidbackend.model.Product;
import com.bidbackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bidPrice(request.getBidPrice())
                .totalBidsRequired(request.getTotalBidsRequired())
                .bidsCompleted(0)
                .imageUrl(request.getImageUrl())
                .status(Product.ProductStatus.OPEN)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getOpenProducts() {
        return productRepository.findByStatus(Product.ProductStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return toResponse(product);
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBidPrice(product.getBidPrice());
        response.setTotalBidsRequired(product.getTotalBidsRequired());
        response.setBidsCompleted(product.getBidsCompleted());
        response.setImageUrl(product.getImageUrl());
        response.setStatus(product.getStatus());
        response.setWinnerId(product.getWinnerId());
        return response;
    }
}
