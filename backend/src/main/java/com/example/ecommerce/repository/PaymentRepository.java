package com.example.ecommerce.repository;

import com.example.ecommerce.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    
    Optional<Payment> findByOrderId(String orderId);
    
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
