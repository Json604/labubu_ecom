package com.example.ecommerce.service;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    public Map<String, Object> createOrder(String userId) {
        // Get cart items
        List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Calculate total and validate stock
        double totalAmount = 0;
        List<Map<String, Object>> orderItemsData = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            Optional<Product> productOpt = productService.getProductById(cartItem.getProductId());
            if (productOpt.isEmpty()) {
                throw new RuntimeException("Product not found: " + cartItem.getProductId());
            }
            
            Product product = productOpt.get();
            
            // Check stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            double itemTotal = product.getPrice() * cartItem.getQuantity();
            totalAmount += itemTotal;
            
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", product.getId());
            itemData.put("quantity", cartItem.getQuantity());
            itemData.put("price", product.getPrice());
            orderItemsData.add(itemData);
        }
        
        // Create order
        Order order = new Order(userId, totalAmount);
        order = orderRepository.save(order);
        
        // Create order items and update stock
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (Map<String, Object> itemData : orderItemsData) {
            OrderItem orderItem = new OrderItem(
                order.getId(),
                (String) itemData.get("productId"),
                (Integer) itemData.get("quantity"),
                (Double) itemData.get("price")
            );
            savedOrderItems.add(mongoTemplate.save(orderItem));
            
            // Reduce stock
            productService.updateStock(
                (String) itemData.get("productId"),
                -((Integer) itemData.get("quantity"))
            );
        }
        
        // Clear cart
        cartService.clearCart(userId);
        
        // Build response
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
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        
        // Get order items
        Query query = new Query(Criteria.where("orderId").is(orderId));
        List<OrderItem> orderItems = mongoTemplate.find(query, OrderItem.class);
        
        // Get payment if exists
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        
        // Build response
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
        
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", payment.getId());
            paymentMap.put("status", payment.getStatus().toString());
            paymentMap.put("amount", payment.getAmount());
            paymentMap.put("razorpayOrderId", payment.getRazorpayOrderId());
            response.put("payment", paymentMap);
        }
        
        return response;
    }
    
    public List<Map<String, Object>> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Order order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus().toString());
            orderMap.put("createdAt", order.getCreatedAt().toString());
            result.add(orderMap);
        }
        
        return result;
    }
    
    public Map<String, Object> cancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        
        // Can't cancel already cancelled orders
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled");
        }
        
        // Restore stock
        Query query = new Query(Criteria.where("orderId").is(orderId));
        List<OrderItem> orderItems = mongoTemplate.find(query, OrderItem.class);
        
        for (OrderItem item : orderItems) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        
        // Build response message based on previous status
        String message = "Order cancelled successfully. Stock restored.";
        if (order.getStatus() == OrderStatus.PAID) {
            message = "Order cancelled. Stock restored. Refund will be processed separately.";
        }
        
        // Update order status
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("status", order.getStatus().toString());
        response.put("message", message);
        
        return response;
    }
    
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        }
    }
    
    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
