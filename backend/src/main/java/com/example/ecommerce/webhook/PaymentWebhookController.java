package com.example.ecommerce.webhook;

import com.example.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Payment webhook handlers")
public class PaymentWebhookController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;
    
    @PostMapping("/payment")
    @Operation(summary = "Razorpay webhook endpoint")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> handlePaymentWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        
        try {
            String event = (String) payload.get("event");
            System.out.println("Received webhook event: " + event);
            
            if ("payment.captured".equals(event) || "payment.authorized".equals(event)) {
                Map<String, Object> payloadData = (Map<String, Object>) payload.get("payload");
                Map<String, Object> paymentEntity = (Map<String, Object>) payloadData.get("payment");
                Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
                
                String razorpayPaymentId = (String) entity.get("id");
                String razorpayOrderId = (String) entity.get("order_id");
                
                paymentService.handlePaymentSuccess(razorpayOrderId, razorpayPaymentId);
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Payment processed successfully");
                return ResponseEntity.ok(response);
                
            } else if ("payment.failed".equals(event)) {
                Map<String, Object> payloadData = (Map<String, Object>) payload.get("payload");
                Map<String, Object> paymentEntity = (Map<String, Object>) payloadData.get("payment");
                Map<String, Object> entity = (Map<String, Object>) paymentEntity.get("entity");
                
                String razorpayOrderId = (String) entity.get("order_id");
                paymentService.handlePaymentFailure(razorpayOrderId);
                
                Map<String, String> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Payment failure recorded");
                return ResponseEntity.ok(response);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "received");
            response.put("event", event);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/payment/test")
    @Operation(summary = "Test webhook for local development")
    public ResponseEntity<?> testWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String razorpayOrderId = (String) payload.get("razorpay_order_id");
            String razorpayPaymentId = (String) payload.get("razorpay_payment_id");
            String status = (String) payload.get("status");
            
            if (razorpayOrderId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "razorpay_order_id is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if ("success".equalsIgnoreCase(status) || status == null) {
                paymentService.handlePaymentSuccess(razorpayOrderId, 
                    razorpayPaymentId != null ? razorpayPaymentId : "test_payment_id");
            } else {
                paymentService.handlePaymentFailure(razorpayOrderId);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "processed");
            response.put("message", "Test webhook processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
