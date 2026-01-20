package com.example.ecommerce.repository;

import com.example.ecommerce.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends MongoRepository<CartItem, String> {
    
    List<CartItem> findByUserId(String userId);
    
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    
    void deleteByUserId(String userId);
}
