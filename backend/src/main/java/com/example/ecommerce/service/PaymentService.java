package com.example.ecommerce.service;

import com.example.ecommerce.client.PaymentServiceClient;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.UserRepository;
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
    
    @Autowired
    private UserRepository userRepository;
    
    public Map<String, Object> createPayment(String orderId) {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BadRequestException("Order is not in CREATED status. Current status: " + order.getStatus());
        }
        
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                throw new BadRequestException("Payment already completed for this order");
            }
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
        
        Double amount = order.getTotalAmount();
        
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
        
        Payment payment = new Payment(orderId, amount);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment = paymentRepository.save(payment);
        
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
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Razorpay order: " + razorpayOrderId));
        
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        paymentRepository.save(payment);
        
        orderService.updateOrderStatus(payment.getOrderId(), OrderStatus.PAID);
        
        // Send confirmation email
        orderService.findById(payment.getOrderId()).ifPresent(order -> {
            userRepository.findById(order.getUserId()).ifPresent(user -> {
                orderService.sendOrderConfirmationEmail(order.getId(), user.getEmail());
            });
        });
    }
    
    public void handlePaymentFailure(String razorpayOrderId) {
        paymentRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        });
    }
    
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
