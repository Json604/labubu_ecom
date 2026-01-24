package com.example.ecommerce.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@labubu.store}")
    private String fromEmail;
    
    @Async
    public void sendOrderConfirmation(String toEmail, String orderId, Double amount) {
        if (mailSender == null) {
            System.out.println("Mail sender not configured. Skipping email to: " + toEmail);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("LABUBU Store - Order Confirmed #" + orderId);
            message.setText(buildOrderConfirmationBody(orderId, amount));
            
            mailSender.send(message);
            System.out.println("Order confirmation email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
    
    private String buildOrderConfirmationBody(String orderId, Double amount) {
        return """
            Thank you for your order at LABUBU Store!
            
            Order ID: %s
            Total Amount: ₹%.2f
            
            Your payment has been confirmed and your order is being processed.
            
            Happy Collecting!
            LABUBU Store Team
            """.formatted(orderId, amount);
    }
}
