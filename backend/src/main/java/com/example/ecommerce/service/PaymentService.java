package com.example.ecommerce.service;

import com.example.ecommerce.client.PaymentServiceClient;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.PaymentRepository;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    public Map<String, Object> createPayment(String orderId, Double amount) {
        // Validate order exists and is in CREATED status
        Optional<Order> orderOpt = orderService.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Order is not in CREATED status. Current status: " + order.getStatus());
        }
        
        // Check if payment already exists for this order
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment already completed for this order");
            }
            // Return existing payment if still pending
            if (payment.getStatus() == PaymentStatus.CREATED) {
                Map<String, Object> response = new HashMap<>();
                response.put("paymentId", payment.getId());
                response.put("orderId", payment.getOrderId());
                response.put("amount", payment.getAmount());
                response.put("status", payment.getStatus().toString());
                response.put("razorpayOrderId", payment.getRazorpayOrderId());
                response.put("razorpayKeyId", paymentServiceClient.getRazorpayKeyId());
                return response;
            }
        }
        
        // Use order total if amount not provided
        if (amount == null) {
            amount = order.getTotalAmount();
        }
        
        // Create Razorpay order
        String razorpayOrderId;
        try {
            com.razorpay.Order razorpayOrder = paymentServiceClient.createRazorpayOrder(
                amount, 
                "order_" + orderId
            );
            razorpayOrderId = razorpayOrder.get("id");
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
        
        // Create payment record
        Payment payment = new Payment(orderId, amount);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment = paymentRepository.save(payment);
        
        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getId());
        response.put("orderId", payment.getOrderId());
        response.put("amount", payment.getAmount());
        response.put("status", payment.getStatus().toString());
        response.put("razorpayOrderId", razorpayOrderId);
        response.put("razorpayKeyId", paymentServiceClient.getRazorpayKeyId());
        
        return response;
    }
    
    public void handlePaymentSuccess(String razorpayOrderId, String razorpayPaymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (paymentOpt.isEmpty()) {
            throw new RuntimeException("Payment not found for Razorpay order: " + razorpayOrderId);
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);
        
        // Update order status to PAID
        orderService.updateOrderStatus(payment.getOrderId(), OrderStatus.PAID);
    }
    
    public void handlePaymentFailure(String razorpayOrderId) {
        Optional<Payment> paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }
    
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
