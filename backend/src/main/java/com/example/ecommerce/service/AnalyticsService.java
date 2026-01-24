package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    public Map<String, Object> getSalesAnalytics(Integer days) {
        if (days == null) days = 30;
        
        Instant startDate = Instant.now().minus(days, ChronoUnit.DAYS);
        
        Query query = new Query(Criteria.where("createdAt").gte(startDate)
                .and("status").is(OrderStatus.PAID));
        List<Order> paidOrders = mongoTemplate.find(query, Order.class);
        
        double totalRevenue = paidOrders.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", paidOrders.size());
        result.put("totalRevenue", totalRevenue);
        result.put("averageOrderValue", paidOrders.isEmpty() ? 0 : totalRevenue / paidOrders.size());
        result.put("periodDays", days);
        result.put("startDate", startDate.toString());
        result.put("endDate", Instant.now().toString());
        
        return result;
    }
    
    public List<Map<String, Object>> getTopProducts(Integer limit) {
        if (limit == null) limit = 5;
        
        // Get all order items from paid orders
        Query orderQuery = new Query(Criteria.where("status").is(OrderStatus.PAID));
        List<Order> paidOrders = mongoTemplate.find(orderQuery, Order.class);
        
        List<String> paidOrderIds = paidOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
        
        Query itemQuery = new Query(Criteria.where("orderId").in(paidOrderIds));
        List<OrderItem> orderItems = mongoTemplate.find(itemQuery, OrderItem.class);
        
        // Aggregate by product
        Map<String, Integer> productSales = new HashMap<>();
        Map<String, Double> productRevenue = new HashMap<>();
        
        for (OrderItem item : orderItems) {
            String productId = item.getProductId();
            productSales.merge(productId, item.getQuantity(), Integer::sum);
            productRevenue.merge(productId, item.getPrice() * item.getQuantity(), Double::sum);
        }
        
        // Sort by quantity sold
        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", entry.getKey());
                    product.put("quantitySold", entry.getValue());
                    product.put("revenue", productRevenue.get(entry.getKey()));
                    return product;
                })
                .collect(Collectors.toList());
        
        return topProducts;
    }
    
    public Map<String, Object> getOrderStatusCounts() {
        Map<String, Object> result = new HashMap<>();
        
        for (OrderStatus status : OrderStatus.values()) {
            Query query = new Query(Criteria.where("status").is(status));
            long count = mongoTemplate.count(query, Order.class);
            result.put(status.name(), count);
        }
        
        result.put("total", orderRepository.count());
        return result;
    }
}
