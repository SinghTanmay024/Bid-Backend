package com.bidbackend.repository;

import com.bidbackend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByStatus(Product.ProductStatus status);
}
