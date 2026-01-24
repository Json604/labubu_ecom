package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
    
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
    
    Page<Product> findByEditionContainingIgnoreCase(String edition, Pageable pageable);
}
