package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddToCartRequest;
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
    
    public CartItem addToCart(AddToCartRequest request) {
        // Check if product exists
        Optional<Product> productOpt = productService.getProductById(request.getProductId());
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found: " + request.getProductId());
        }
        
        Product product = productOpt.get();
        
        // Check if product has enough stock
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }
        
        // Check if item already in cart
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(
            request.getUserId(), request.getProductId());
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check stock for new quantity
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
            }
            
            cartItem.setQuantity(newQuantity);
            return cartRepository.save(cartItem);
        } else {
            // Add new item
            CartItem cartItem = new CartItem(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
            );
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
            
            // Get product details
            Optional<Product> productOpt = productService.getProductById(item.getProductId());
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", product.getId());
                productInfo.put("name", product.getName());
                productInfo.put("price", product.getPrice());
                productInfo.put("edition", product.getEdition());
                cartItemWithProduct.put("product", productInfo);
            }
            
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
