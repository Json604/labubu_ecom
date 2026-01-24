package com.example.ecommerce.service;

import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Autowired
    private ProductRepository productRepository;
    
    public String uploadProductImage(String productId, MultipartFile file) throws IOException {
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        
        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = productId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());
        
        // Update product with image path
        product.setImagePath(filename);
        productRepository.save(product);
        
        return filename;
    }
    
    public byte[] getProductImage(String productId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        if (product.getImagePath() == null) {
            throw new ResourceNotFoundException("Image not found for product: " + productId);
        }
        
        Path filePath = Paths.get(uploadDir).resolve(product.getImagePath());
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Image file not found");
        }
        
        return Files.readAllBytes(filePath);
    }
}
