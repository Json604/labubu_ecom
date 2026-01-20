# 🧸 LABUBU Toy Store - E-Commerce Backend API

A minimal e-commerce backend system for a LABUBU collectible toy store built with Spring Boot, MongoDB, and Razorpay payment integration.

## 📋 Table of Contents

1. [What This Project Does](#what-this-project-does)
2. [High-Level Flow](#high-level-flow)
3. [Folder Structure](#folder-structure)
4. [MongoDB Setup](#mongodb-setup)
5. [Razorpay Setup](#razorpay-setup)
6. [Environment Variables](#environment-variables)
7. [Running the Application](#running-the-application)
8. [Step-by-Step Postman Testing](#step-by-step-postman-testing)
9. [Internal Flow Explanation](#internal-flow-explanation)
10. [Known Limitations](#known-limitations)

---

## 🎯 What This Project Does

This is a backend API for a LABUBU toy store that allows:

- **Product Management**: Create and list LABUBU collectible variants (different colors, editions, prices)
- **Shopping Cart**: Add items to cart, view cart, clear cart
- **Order Processing**: Create orders from cart items, view order details, cancel orders
- **Payment Integration**: Real Razorpay payment processing with webhook support

**Target Users**: Collectors who want to purchase LABUBU toys online.

---

## 🔄 High-Level Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Create    │────▶│   Add to    │────▶│   Create    │────▶│   Create    │
│  Products   │     │    Cart     │     │    Order    │     │   Payment   │
└─────────────┘     └─────────────┘     └─────────────┘     └──────┬──────┘
                                                                    │
                                                                    ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Order     │◀────│   Update    │◀────│   Webhook   │◀────│  Razorpay   │
│  Confirmed  │     │   Status    │     │  Callback   │     │  Payment    │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

**Complete Flow:**
1. Admin creates LABUBU products with name, edition, price, and stock
2. User adds products to their cart
3. User creates an order from cart (cart is cleared, stock is reduced)
4. User initiates payment (Razorpay order is created)
5. User completes payment on Razorpay
6. Razorpay sends webhook to our server
7. Order status is updated to PAID

---

## 📁 Folder Structure

```
com.example.ecommerce
│
├── controller/
│   ├── ProductController.java    # Product CRUD endpoints
│   ├── CartController.java       # Cart management endpoints
│   ├── OrderController.java      # Order management endpoints
│   └── PaymentController.java    # Payment creation endpoint
│
├── service/
│   ├── ProductService.java       # Product business logic
│   ├── CartService.java          # Cart business logic
│   ├── OrderService.java         # Order business logic
│   └── PaymentService.java       # Payment business logic
│
├── repository/
│   ├── ProductRepository.java    # MongoDB product operations
│   ├── CartRepository.java       # MongoDB cart operations
│   ├── OrderRepository.java      # MongoDB order operations
│   └── PaymentRepository.java    # MongoDB payment operations
│
├── model/
│   ├── User.java                 # User entity
│   ├── Product.java              # Product entity
│   ├── CartItem.java             # Cart item entity
│   ├── Order.java                # Order entity
│   ├── OrderItem.java            # Order item entity
│   ├── Payment.java              # Payment entity
│   ├── OrderStatus.java          # Enum: CREATED, PAID, CANCELLED
│   └── PaymentStatus.java        # Enum: CREATED, SUCCESS, FAILED
│
├── dto/
│   ├── AddToCartRequest.java     # Request body for adding to cart
│   ├── CreateOrderRequest.java   # Request body for creating order
│   ├── PaymentRequest.java       # Request body for creating payment
│   └── PaymentWebhookRequest.java # Webhook payload structure
│
├── webhook/
│   └── PaymentWebhookController.java  # Razorpay webhook handler
│
├── client/
│   └── PaymentServiceClient.java # Razorpay API client
│
├── config/
│   └── RestTemplateConfig.java   # REST client configuration
│
└── EcommerceApplication.java     # Main application entry point
```

---

## 🗄️ MongoDB Setup

### Option 1: MongoDB Atlas (Cloud - Recommended)

1. Go to [MongoDB Atlas](https://www.mongodb.com/atlas)
2. Create a free account and cluster
3. Create a database user with password
4. Get your connection string (looks like):
   ```
   mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
5. Whitelist your IP address (or use 0.0.0.0/0 for testing)

### Option 2: Local MongoDB

1. Install MongoDB Community Edition
2. Start MongoDB service:
   ```bash
   # macOS
   brew services start mongodb-community
   
   # Linux
   sudo systemctl start mongod
   
   # Windows
   net start MongoDB
   ```
3. Connection string: `mongodb://localhost:27017`

### Database Collections (Auto-created)

- `products` - LABUBU product catalog
- `cart_items` - User shopping carts
- `orders` - Customer orders
- `order_items` - Individual items in orders
- `payments` - Payment records

---

## 💳 Razorpay Setup

### 1. Create Razorpay Account

1. Go to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Sign up for a free account
3. Complete KYC (or use Test Mode)

### 2. Get API Keys

1. Go to Settings → API Keys
2. Generate a new key pair
3. Copy:
   - **Key ID**: `rzp_test_xxxxxxxxxxxx`
   - **Key Secret**: `xxxxxxxxxxxxxxxxxxxxxxxx`

### 3. Configure Webhook (Optional for Testing)

1. Go to Settings → Webhooks
2. Add new webhook:
   - URL: `https://your-domain.com/api/webhooks/payment`
   - Events: `payment.captured`, `payment.failed`
3. Copy the Webhook Secret

**Note**: For local testing, use the `/api/webhooks/payment/test` endpoint to simulate webhooks.

---

## 🔐 Environment Variables

Create a `.env` file or set these environment variables:

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `MONGODB_URI` | MongoDB connection string | Yes | `mongodb://localhost:27017` |
| `MONGODB_DATABASE` | Database name | Yes | `labubu_store` |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID | Yes | - |
| `RAZORPAY_KEY_SECRET` | Razorpay API Key Secret | Yes | - |
| `RAZORPAY_WEBHOOK_SECRET` | Webhook signature secret | No | - |
| `SERVER_PORT` | Application port | No | `8080` |

### What Breaks If Missing

| Variable | Impact |
|----------|--------|
| `MONGODB_URI` | Application fails to start - cannot connect to database |
| `MONGODB_DATABASE` | Uses default database name, data may be in wrong place |
| `RAZORPAY_KEY_ID` | Payment creation fails with authentication error |
| `RAZORPAY_KEY_SECRET` | Payment creation fails with authentication error |
| `RAZORPAY_WEBHOOK_SECRET` | Webhook signature verification skipped (less secure) |
| `SERVER_PORT` | Uses default port 8080 |

### Setting Environment Variables

**macOS/Linux:**
```bash
export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net"
export MONGODB_DATABASE="labubu_store"
export RAZORPAY_KEY_ID="rzp_test_xxxx"
export RAZORPAY_KEY_SECRET="xxxxxxxxxxxx"
export SERVER_PORT=8080
```

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net"
$env:MONGODB_DATABASE="labubu_store"
$env:RAZORPAY_KEY_ID="rzp_test_xxxx"
$env:RAZORPAY_KEY_SECRET="xxxxxxxxxxxx"
$env:SERVER_PORT=8080
```

---

## 🚀 Running the Application

### Prerequisites

- Java 17+
- Maven 3.6+
- MongoDB (local or Atlas)
- Razorpay account

### Steps

1. **Clone and navigate to project:**
   ```bash
   cd ecommerce
   ```

2. **Set environment variables** (see above)

3. **Build the project:**
   ```bash
   ./mvnw clean install
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Verify it's running:**
   ```bash
   curl http://localhost:8080/api/products
   ```
   Should return `[]` (empty array)

---

## 🧪 Step-by-Step Postman Testing

### Setup Postman

1. Create a new Collection called "LABUBU Store"
2. Create environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `userId`: `user123`
   - `productId`: (will be set after creating product)
   - `orderId`: (will be set after creating order)
   - `razorpayOrderId`: (will be set after creating payment)

---

### Step 1: Create LABUBU Products

**Request:**
```
POST {{baseUrl}}/api/products
Content-Type: application/json

{
  "name": "LABUBU Pink Classic",
  "description": "Original pink LABUBU collectible figure",
  "edition": "Classic Series",
  "price": 1299.00,
  "stock": 50
}
```

**Response:**
```json
{
  "id": "6789abc...",
  "name": "LABUBU Pink Classic",
  "description": "Original pink LABUBU collectible figure",
  "edition": "Classic Series",
  "price": 1299.0,
  "stock": 50
}
```

**Save the `id` as `productId` variable.**

Create more products:
```json
// Blue Limited Edition
{
  "name": "LABUBU Blue Limited",
  "description": "Limited edition blue LABUBU",
  "edition": "Limited Series 2024",
  "price": 2499.00,
  "stock": 20
}

// Gold Special
{
  "name": "LABUBU Gold Special",
  "description": "Special gold variant LABUBU",
  "edition": "Special Collection",
  "price": 4999.00,
  "stock": 10
}
```

---

### Step 2: List All Products

**Request:**
```
GET {{baseUrl}}/api/products
```

**Response:**
```json
[
  {
    "id": "6789abc...",
    "name": "LABUBU Pink Classic",
    "edition": "Classic Series",
    "price": 1299.0,
    "stock": 50
  },
  ...
]
```

---

### Step 3: Search Products (Bonus Feature)

**Request:**
```
GET {{baseUrl}}/api/products/search?q=pink
```

**Response:**
```json
[
  {
    "id": "6789abc...",
    "name": "LABUBU Pink Classic",
    ...
  }
]
```

---

### Step 4: Add Products to Cart

**Request:**
```
POST {{baseUrl}}/api/cart/add
Content-Type: application/json

{
  "userId": "{{userId}}",
  "productId": "{{productId}}",
  "quantity": 2
}
```

**Response:**
```json
{
  "id": "cart123...",
  "userId": "user123",
  "productId": "6789abc...",
  "quantity": 2
}
```

---

### Step 5: View Cart

**Request:**
```
GET {{baseUrl}}/api/cart/{{userId}}
```

**Response:**
```json
[
  {
    "id": "cart123...",
    "productId": "6789abc...",
    "quantity": 2,
    "product": {
      "id": "6789abc...",
      "name": "LABUBU Pink Classic",
      "price": 1299.0,
      "edition": "Classic Series"
    }
  }
]
```

---

### Step 6: Create Order from Cart

**Request:**
```
POST {{baseUrl}}/api/orders
Content-Type: application/json

{
  "userId": "{{userId}}"
}
```

**Response:**
```json
{
  "id": "order456...",
  "userId": "user123",
  "totalAmount": 2598.0,
  "status": "CREATED",
  "createdAt": "2024-01-15T10:30:00Z",
  "items": [
    {
      "productId": "6789abc...",
      "quantity": 2,
      "price": 1299.0
    }
  ]
}
```

**Save the `id` as `orderId` variable.**

**Note:** Cart is automatically cleared after order creation.

---

### Step 7: Verify Cart is Empty

**Request:**
```
GET {{baseUrl}}/api/cart/{{userId}}
```

**Response:**
```json
[]
```

---

### Step 8: Create Payment (Razorpay Order)

**Request:**
```
POST {{baseUrl}}/api/payments/create
Content-Type: application/json

{
  "orderId": "{{orderId}}",
  "amount": 2598.0
}
```

**Response:**
```json
{
  "paymentId": "pay789...",
  "orderId": "order456...",
  "amount": 2598.0,
  "status": "CREATED",
  "razorpayOrderId": "order_xyz123...",
  "razorpayKeyId": "rzp_test_xxxx"
}
```

**Save `razorpayOrderId` variable.**

---

### Step 9: Simulate Payment Webhook (Test Endpoint)

Since we can't complete actual Razorpay payment in Postman, use the test webhook:

**Request:**
```
POST {{baseUrl}}/api/webhooks/payment/test
Content-Type: application/json

{
  "razorpay_order_id": "{{razorpayOrderId}}",
  "razorpay_payment_id": "pay_test123",
  "status": "success"
}
```

**Response:**
```json
{
  "status": "processed",
  "message": "Test webhook processed successfully"
}
```

---

### Step 10: Verify Order Status Updated

**Request:**
```
GET {{baseUrl}}/api/orders/{{orderId}}
```

**Response:**
```json
{
  "id": "order456...",
  "userId": "user123",
  "totalAmount": 2598.0,
  "status": "PAID",
  "createdAt": "2024-01-15T10:30:00Z",
  "items": [...],
  "payment": {
    "id": "pay789...",
    "status": "SUCCESS",
    "amount": 2598.0,
    "razorpayOrderId": "order_xyz123..."
  }
}
```

**✅ Order status is now PAID!**

---

### Bonus: Order History

**Request:**
```
GET {{baseUrl}}/api/orders/user/{{userId}}
```

**Response:**
```json
[
  {
    "id": "order456...",
    "totalAmount": 2598.0,
    "status": "PAID",
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

---

### Bonus: Cancel Order (Only if not paid)

**Request:**
```
POST {{baseUrl}}/api/orders/{{orderId}}/cancel
```

**Response (if order is CREATED):**
```json
{
  "id": "order456...",
  "status": "CANCELLED",
  "message": "Order cancelled successfully. Stock restored."
}
```

**Response (if order is PAID):**
```json
{
  "error": "Cannot cancel a paid order"
}
```

---

### Bonus: Clear Cart

**Request:**
```
DELETE {{baseUrl}}/api/cart/{{userId}}/clear
```

**Response:**
```json
{
  "message": "Cart cleared successfully"
}
```

---

## 🔧 Internal Flow Explanation

### 1. Add to Cart Flow

```
Request → CartController → CartService
                              ↓
                    Check product exists (ProductService)
                              ↓
                    Check stock available
                              ↓
                    Check if item already in cart
                              ↓
                    Update quantity OR Create new CartItem
                              ↓
                    Save to MongoDB (CartRepository)
```

### 2. Create Order Flow

```
Request → OrderController → OrderService
                              ↓
                    Get cart items (CartService)
                              ↓
                    Validate cart not empty
                              ↓
                    Calculate total amount
                              ↓
                    Check stock for all items
                              ↓
                    Create Order (status: CREATED)
                              ↓
                    Create OrderItems for each cart item
                              ↓
                    Reduce product stock (ProductService)
                              ↓
                    Clear cart (CartService)
                              ↓
                    Return order with items
```

### 3. Payment Flow

```
Request → PaymentController → PaymentService
                                  ↓
                        Validate order exists
                                  ↓
                        Check order status is CREATED
                                  ↓
                        Create Razorpay order (PaymentServiceClient)
                                  ↓
                        Save Payment record (status: CREATED)
                                  ↓
                        Return payment details with razorpayOrderId
```

### 4. Webhook Flow

```
Razorpay → PaymentWebhookController
                    ↓
            Parse webhook payload
                    ↓
            Extract razorpayOrderId and paymentId
                    ↓
            PaymentService.handlePaymentSuccess()
                    ↓
            Update Payment status to SUCCESS
                    ↓
            Update Order status to PAID (OrderService)
```

---

## ⚠️ Known Limitations (Intentional)

This project is intentionally NOT production-grade. The following limitations exist by design:

### Security
- **No Authentication**: userId is passed directly in requests
- **No JWT/OAuth**: Anyone can access any user's data
- **No Input Validation**: Minimal validation on request bodies
- **Webhook Signature**: Verification is optional/skipped

### Architecture
- **No Microservices**: Single monolithic application
- **No Message Queues**: Synchronous processing only
- **No Caching**: No Redis or in-memory caching
- **No Rate Limiting**: APIs can be called unlimited times

### Data
- **No User Management**: Users are just string IDs
- **No Inventory Locking**: Race conditions possible on stock
- **No Transaction Support**: Operations are not atomic
- **No Soft Deletes**: Data is permanently deleted

### Features
- **No Frontend**: API only, tested via Postman
- **No Email Notifications**: No order confirmations
- **No Refunds**: Payment cancellation not implemented
- **No Shipping**: No delivery tracking

### Error Handling
- **Basic Exceptions**: RuntimeException used everywhere
- **No Custom Error Codes**: Generic error messages
- **No Logging Framework**: Uses System.out.println

---

## 📚 API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create a product |
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/search?q=` | Search products |
| POST | `/api/cart/add` | Add item to cart |
| GET | `/api/cart/{userId}` | Get user's cart |
| DELETE | `/api/cart/{userId}/clear` | Clear cart |
| POST | `/api/orders` | Create order from cart |
| GET | `/api/orders/{orderId}` | Get order details |
| GET | `/api/orders/user/{userId}` | Get user's order history |
| POST | `/api/orders/{orderId}/cancel` | Cancel order |
| POST | `/api/payments/create` | Create Razorpay payment |
| POST | `/api/webhooks/payment` | Razorpay webhook |
| POST | `/api/webhooks/payment/test` | Test webhook (manual) |

---

## 🎓 Learning Outcomes

This project demonstrates:

1. **Spring Boot REST API Development**
2. **MongoDB with Spring Data**
3. **Third-party Payment Integration (Razorpay)**
4. **Webhook Pattern Implementation**
5. **Service Layer Architecture**
6. **DTO Pattern for Request/Response**

---

Built for educational purposes. Not for production use.
