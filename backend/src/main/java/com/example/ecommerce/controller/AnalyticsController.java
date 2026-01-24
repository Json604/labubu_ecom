package com.example.ecommerce.controller;

import com.example.ecommerce.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Sales and order analytics (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/sales")
    @Operation(summary = "Get sales analytics", description = "Returns total orders, revenue, and average order value")
    public ResponseEntity<Map<String, Object>> getSalesAnalytics(
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        return ResponseEntity.ok(analyticsService.getSalesAnalytics(days));
    }
    
    @GetMapping("/products/top")
    @Operation(summary = "Get top selling products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(limit));
    }
    
    @GetMapping("/orders/status")
    @Operation(summary = "Get order counts by status")
    public ResponseEntity<Map<String, Object>> getOrderStatusCounts() {
        return ResponseEntity.ok(analyticsService.getOrderStatusCounts());
    }
}
