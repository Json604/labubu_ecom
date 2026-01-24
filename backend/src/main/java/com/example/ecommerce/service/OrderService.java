package com.example.ecommerce.service;

import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private EmailUtil emailUtil;
    
    public Map<String, Object> createOrder(String userId) {
        List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);
        
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        
        double totalAmount = 0;
        List<Map<String, Object>> orderItemsData = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Product product = productService.getProductById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", cartItem.getProductId()));
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            
            double itemTotal = product.getPrice() * cartItem.getQuantity();
            totalAmount += itemTotal;
            
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", product.getId());
            itemData.put("quantity", cartItem.getQuantity());
            itemData.put("price", product.getPrice());
            orderItemsData.add(itemData);
        }
        
        Order order = new Order(userId, totalAmount);
        order = orderRepository.save(order);
        
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (Map<String, Object> itemData : orderItemsData) {
            OrderItem orderItem = new OrderItem(
                order.getId(),
                (String) itemData.get("productId"),
                (Integer) itemData.get("quantity"),
                (Double) itemData.get("price")
            );
            savedOrderItems.add(mongoTemplate.save(orderItem));
            
            productService.updateStock(
                (String) itemData.get("productId"),
                -((Integer) itemData.get("quantity"))
            );
        }
        
        cartService.clearCart(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("userId", order.getUserId());
        response.put("totalAmount", order.getTotalAmount());
        response.put("status", order.getStatus().toString());
        response.put("createdAt", order.getCreatedAt().toString());
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : savedOrderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice());
            items.add(itemMap);
        }
        response.put("items", items);
        
        return response;
    }
    
    public Map<String, Object> getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        Query query = new Query(Criteria.where("orderId").is(orderId));
        List<OrderItem> orderItems = mongoTemplate.find(query, OrderItem.class);
        
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("userId", order.getUserId());
        response.put("totalAmount", order.getTotalAmount());
        response.put("status", order.getStatus().toString());
        response.put("createdAt", order.getCreatedAt().toString());
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice());
            items.add(itemMap);
        }
        response.put("items", items);
        
        paymentOpt.ifPresent(payment -> {
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", payment.getId());
            paymentMap.put("status", payment.getStatus().toString());
            paymentMap.put("amount", payment.getAmount());
            paymentMap.put("razorpayOrderId", payment.getRazorpayOrderId());
            response.put("payment", paymentMap);
        });
        
        return response;
    }
    
    public Page<Order> getOrdersByUserId(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }
    
    public Map<String, Object> cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }
        
        Query query = new Query(Criteria.where("orderId").is(orderId));
        List<OrderItem> orderItems = mongoTemplate.find(query, OrderItem.class);
        
        for (OrderItem item : orderItems) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        
        String message = "Order cancelled successfully. Stock restored.";
        if (order.getStatus() == OrderStatus.PAID) {
            message = "Order cancelled. Stock restored. Refund will be processed separately.";
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("status", order.getStatus().toString());
        response.put("message", message);
        
        return response;
    }
    
    public void updateOrderStatus(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
    
    public void sendOrderConfirmationEmail(String orderId, String userEmail) {
        orderRepository.findById(orderId).ifPresent(order -> {
            emailUtil.sendOrderConfirmation(userEmail, orderId, order.getTotalAmount());
        });
    }
    
    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
