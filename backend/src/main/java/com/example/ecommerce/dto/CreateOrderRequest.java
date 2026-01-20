package com.example.ecommerce.dto;

public class CreateOrderRequest {
    
    private String userId;
    
    public CreateOrderRequest() {
    }
    
    public CreateOrderRequest(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
