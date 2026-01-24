package com.example.ecommerce.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    private String name;
    private String description;
    private String edition;
    private Double price;
    private Integer stock;
    private String imagePath;
    
    public Product() {
    }
    
    public Product(String name, String description, String edition, Double price, Integer stock) {
        this.name = name;
        this.description = description;
        this.edition = edition;
        this.price = price;
        this.stock = stock;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
