package com.bidbackend.controller;

import com.bidbackend.dto.ProductRequest;
import com.bidbackend.dto.ProductResponse;
import com.bidbackend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Manage bid products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product", description = "Add a new product available for bidding")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Returns all products regardless of status")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/open")
    @Operation(summary = "List open products", description = "Returns only products still open for bidding")
    public ResponseEntity<List<ProductResponse>> getOpenProducts() {
        return ResponseEntity.ok(productService.getOpenProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns a single product with current bid count and status")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
