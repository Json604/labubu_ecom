package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddToCartRequest;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<?> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication auth) {
        try {
            String userId = auth.getName();
            CartItem cartItem = cartService.addToCart(userId, request);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<List<Map<String, Object>>> getCart(Authentication auth) {
        String userId = auth.getName();
        List<Map<String, Object>> cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart")
    public ResponseEntity<Map<String, String>> clearCart(Authentication auth) {
        String userId = auth.getName();
        cartService.clearCart(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cart cleared successfully");
        return ResponseEntity.ok(response);
    }
}
