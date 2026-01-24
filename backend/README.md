# LABUBU Store - Backend API

Production-grade Spring Boot backend for LABUBU collectible toy e-commerce.

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- MongoDB
- Razorpay Payment Gateway
- Swagger/OpenAPI

## Features

### Core Features
- Product CRUD with pagination, sorting, filtering
- Shopping cart management
- Order processing with stock management
- Razorpay payment integration
- Order cancellation with refund support

### Security
- JWT authentication
- Role-based access control (USER/ADMIN)
- Password hashing (BCrypt)
- API rate limiting (100 req/min per IP)

### Advanced Features
- Input validation (Jakarta Validation)
- Global exception handling
- Caching (product listings)
- File upload (product images)
- Email notifications (order confirmation)
- Analytics APIs (sales, top products, order stats)

## API Documentation

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Running

```bash
export MONGODB_URI="your-mongodb-uri"
export RAZORPAY_KEY_ID="your-key"
export RAZORPAY_KEY_SECRET="your-secret"
./mvnw spring-boot:run
```

---

## API Testing with cURL (Demo Commands)

### 1. Authentication

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'
```

**Login (save the token!):**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Save token for subsequent requests:**
```bash
TOKEN="paste-your-token-here"
```

---

### 2. Products (Public GET, Admin POST/PUT/DELETE)

**List products with pagination:**
```bash
curl "http://localhost:8080/api/products?page=0&size=10"
```

**List products with filtering:**
```bash
curl "http://localhost:8080/api/products?minPrice=1000&maxPrice=3000&edition=Classic"
```

**List products with sorting:**
```bash
curl "http://localhost:8080/api/products?sortBy=price&sortDir=desc"
```

**Search products:**
```bash
curl "http://localhost:8080/api/products/search?q=pink"
```

**Get single product:**
```bash
curl http://localhost:8080/api/products/PRODUCT_ID
```

**Create product (Admin only):**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"LABUBU Pink Classic","description":"Original pink edition","edition":"Classic Series","price":1299,"stock":50}'
```

**Create more products:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"LABUBU Blue Limited","description":"Limited blue edition","edition":"Limited 2024","price":2499,"stock":20}'

curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"LABUBU Gold Special","description":"Special gold variant","edition":"Special Collection","price":4999,"stock":10}'
```

**Update product (Admin only):**
```bash
curl -X PUT http://localhost:8080/api/products/PRODUCT_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"LABUBU Pink Classic V2","description":"Updated edition","edition":"Classic Series","price":1399,"stock":45}'
```

**Delete product (Admin only):**
```bash
curl -X DELETE http://localhost:8080/api/products/PRODUCT_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Upload product image (Admin only):**
```bash
curl -X POST http://localhost:8080/api/products/PRODUCT_ID/image \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/path/to/image.jpg"
```

---

### 3. Cart (Authenticated)

**Add to cart:**
```bash
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"productId":"PRODUCT_ID","quantity":2}'
```

**View cart:**
```bash
curl http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN"
```

**Clear cart:**
```bash
curl -X DELETE http://localhost:8080/api/cart/clear \
  -H "Authorization: Bearer $TOKEN"
```

---

### 4. Orders (Authenticated)

**Create order from cart:**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

**List user's orders:**
```bash
curl "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Get order details:**
```bash
curl http://localhost:8080/api/orders/ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Cancel order:**
```bash
curl -X POST http://localhost:8080/api/orders/ORDER_ID/cancel \
  -H "Authorization: Bearer $TOKEN"
```

---

### 5. Payments (Authenticated)

**Create Razorpay payment:**
```bash
curl -X POST http://localhost:8080/api/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"orderId":"ORDER_ID"}'
```

**Simulate payment success (test webhook):**
```bash
curl -X POST http://localhost:8080/api/webhooks/payment/test \
  -H "Content-Type: application/json" \
  -d '{"razorpay_order_id":"RAZORPAY_ORDER_ID","status":"success"}'
```

---

### 6. Analytics (Admin only)

**Get sales analytics:**
```bash
curl "http://localhost:8080/api/analytics/sales?days=30" \
  -H "Authorization: Bearer $TOKEN"
```

**Get top selling products:**
```bash
curl "http://localhost:8080/api/analytics/products/top?limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

**Get order status counts:**
```bash
curl http://localhost:8080/api/analytics/orders/status \
  -H "Authorization: Bearer $TOKEN"
```

---

### 7. Validation & Error Handling Demo

**Invalid registration (validation error):**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"A","email":"invalid","password":"123"}'
```

**Unauthorized access (no token):**
```bash
curl http://localhost:8080/api/cart
```

**Resource not found:**
```bash
curl http://localhost:8080/api/products/nonexistent123 \
  -H "Authorization: Bearer $TOKEN"
```

---

### 8. Rate Limiting Demo

**Send many requests quickly (will get 429 after limit):**
```bash
for i in {1..110}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/products; done
```

---

## Complete Demo Flow

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Demo User","email":"demo@test.com","password":"demo123"}'

# 2. Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"demo123"}' | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"

# 3. Create product (need ADMIN role - update in MongoDB first)
# db.users.updateOne({email:"demo@test.com"},{$set:{role:"ADMIN"}})

# 4. List products
curl "http://localhost:8080/api/products"

# 5. Add to cart
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"productId":"PRODUCT_ID","quantity":2}'

# 6. View cart
curl http://localhost:8080/api/cart -H "Authorization: Bearer $TOKEN"

# 7. Create order
curl -X POST http://localhost:8080/api/orders -H "Authorization: Bearer $TOKEN"

# 8. Create payment
curl -X POST http://localhost:8080/api/payments/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"orderId":"ORDER_ID"}'

# 9. Simulate payment success
curl -X POST http://localhost:8080/api/webhooks/payment/test \
  -H "Content-Type: application/json" \
  -d '{"razorpay_order_id":"RAZORPAY_ORDER_ID","status":"success"}'

# 10. Check order status (should be PAID)
curl http://localhost:8080/api/orders/ORDER_ID -H "Authorization: Bearer $TOKEN"

# 11. View analytics (Admin)
curl http://localhost:8080/api/analytics/sales -H "Authorization: Bearer $TOKEN"
```

---

## Making a User Admin

After registering, run this in MongoDB:
```javascript
db.users.updateOne(
  { email: "demo@test.com" },
  { $set: { role: "ADMIN" } }
)
```

---

## Folder Structure

```
com.example.ecommerce/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # MongoDB data access
├── model/           # Entity classes
├── dto/             # Request/Response objects
├── exception/       # Custom exceptions + handler
├── security/        # JWT filter, Security config
├── util/            # JWT, Email utilities
├── config/          # OpenAPI config
├── client/          # Razorpay client
└── webhook/         # Payment webhooks
```

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| MONGODB_URI | Yes | localhost:27017 | MongoDB connection string |
| MONGODB_DATABASE | No | labubu_store | Database name |
| RAZORPAY_KEY_ID | Yes | - | Razorpay API key |
| RAZORPAY_KEY_SECRET | Yes | - | Razorpay secret |
| JWT_SECRET | No | (default) | JWT signing key |
| MAIL_HOST | No | smtp.gmail.com | SMTP host |
| MAIL_USERNAME | No | - | Email username |
| MAIL_PASSWORD | No | - | Email password |
