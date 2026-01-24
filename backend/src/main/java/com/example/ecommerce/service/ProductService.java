package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Cacheable(value = "products", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public Page<Product> getProductsFiltered(Double minPrice, Double maxPrice, 
                                              String edition, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        
        if (minPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        }
        if (maxPrice != null) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }
        if (edition != null && !edition.isEmpty()) {
            criteriaList.add(Criteria.where("edition").regex(edition, "i"));
        }
        
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        query.with(pageable);
        
        List<Product> products = mongoTemplate.find(query, Product.class);
        
        Query countQuery = new Query();
        if (!criteriaList.isEmpty()) {
            countQuery.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        
        return PageableExecutionUtils.getPage(products, pageable, 
                () -> mongoTemplate.count(countQuery, Product.class));
    }
    
    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }
    
    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(query, pageable);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setEdition(request.getEdition());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        return productRepository.save(product);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }
    
    public Product updateStock(String productId, int quantityChange) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        
        int newStock = product.getStock() + quantityChange;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }
    
    public boolean hasStock(String productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        return productOpt.map(p -> p.getStock() >= quantity).orElse(false);
    }
}
