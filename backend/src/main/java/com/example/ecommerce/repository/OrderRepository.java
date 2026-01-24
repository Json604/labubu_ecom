package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    
    Page<Order> findByUserId(String userId, Pageable pageable);
}
