package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.FileService;
import com.example.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private FileService fileService;
    
    @PostMapping
    @Operation(summary = "Create product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setEdition(request.getEdition());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        Product created = productService.createProduct(product);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping
    @Operation(summary = "List all products with pagination and filtering")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String edition) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        Page<Product> productPage = productService.getProductsFiltered(
                minPrice, maxPrice, edition, pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalElements", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.getProductById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<?> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> results = productService.searchProducts(q, pageRequest);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", results.getContent());
        response.put("currentPage", results.getNumber());
        response.put("totalElements", results.getTotalElements());
        response.put("totalPages", results.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id, 
            @Valid @RequestBody ProductRequest request) {
        Product updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/image")
    @Operation(summary = "Upload product image (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileService.uploadProductImage(id, file);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image uploaded successfully");
        response.put("filename", filename);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/image")
    @Operation(summary = "Get product image")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) throws IOException {
        byte[] image = fileService.getProductImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }
}
