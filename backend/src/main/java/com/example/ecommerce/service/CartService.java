package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductService productService;
    
    public CartItem addToCart(String userId, AddToCartRequest request) {
        // Check if product exists
        Product product = productService.getProductById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        
        // Check if product has enough stock
        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }
        
        // Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(
            userId, request.getProductId());
        
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
            }
            
            cartItem.setQuantity(newQuantity);
            return cartRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem(userId, request.getProductId(), request.getQuantity());
            return cartRepository.save(cartItem);
        }
    }
    
    public List<Map<String, Object>> getCartByUserId(String userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (CartItem item : cartItems) {
            Map<String, Object> cartItemWithProduct = new HashMap<>();
            cartItemWithProduct.put("id", item.getId());
            cartItemWithProduct.put("productId", item.getProductId());
            cartItemWithProduct.put("quantity", item.getQuantity());
            
            productService.getProductById(item.getProductId()).ifPresent(product -> {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", product.getId());
                productInfo.put("name", product.getName());
                productInfo.put("price", product.getPrice());
                productInfo.put("edition", product.getEdition());
                cartItemWithProduct.put("product", productInfo);
            });
            
            result.add(cartItemWithProduct);
        }
        
        return result;
    }
    
    public List<CartItem> getCartItemsByUserId(String userId) {
        return cartRepository.findByUserId(userId);
    }
    
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
