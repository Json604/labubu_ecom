package com.example.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.mongodb.uri=mongodb://localhost:27017",
    "spring.data.mongodb.database=labubu_store_test",
    "razorpay.key.id=test_key",
    "razorpay.key.secret=test_secret"
})
class EcommerceApplicationTests {

    @Test
    void contextLoads() {
        // Basic context load test
    }
}
