package com.example.ecommerce.client;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceClient {
    
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    private RazorpayClient razorpayClient;
    
    // Lazy initialization of Razorpay client
    private RazorpayClient getClient() throws RazorpayException {
        if (razorpayClient == null) {
            razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        }
        return razorpayClient;
    }
    
    /**
     * Creates a Razorpay order for the given amount
     * Amount should be in rupees, will be converted to paise
     */
    public Order createRazorpayOrder(Double amount, String receipt) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        // Razorpay expects amount in paise (smallest currency unit)
        orderRequest.put("amount", (int)(amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        
        return getClient().orders.create(orderRequest);
    }
    
    /**
     * Fetches a Razorpay order by ID
     */
    public Order fetchRazorpayOrder(String razorpayOrderId) throws RazorpayException {
        return getClient().orders.fetch(razorpayOrderId);
    }
    
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}
