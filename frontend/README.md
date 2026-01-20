# LABUBU Store Frontend

Minimal React frontend for the LABUBU toy e-commerce store.

## Tech Stack

- React 18 + Vite
- React Router for navigation
- Axios for API calls
- Plain CSS (brutalist style)

## How to Run

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

## Backend Connection

The frontend expects the backend to be running on `http://localhost:8080`.

Start the backend first:
```bash
cd backend
./mvnw spring-boot:run
```

## User Flow

1. **Browse Products** - View all LABUBU collectibles on the home page
2. **Add to Cart** - Select quantity and add items to cart
3. **View Cart** - Review items and total price
4. **Create Order** - Convert cart to order (cart is cleared)
5. **Pay Now** - Initiate Razorpay payment
6. **Payment Complete** - Order status updates to PAID

## Pages

| Route | Description |
|-------|-------------|
| `/` | Product listing |
| `/cart` | Shopping cart |
| `/orders` | Order history |
| `/order/:id` | Order details + payment |
| `/payment/:id` | Payment result |

## Assumptions

- User ID is hardcoded as `user123`
- No authentication required
- Backend handles all business logic
- Payment is simulated via test webhook endpoint

## Known Limitations

- No real Razorpay checkout UI (uses test webhook)
- No product images
- No user registration/login
- No search functionality in UI
- Cart quantity can't be edited (only add more)
- No responsive design optimizations

## API Endpoints Used

```
GET  /api/products
POST /api/cart/add
GET  /api/cart/{userId}
DELETE /api/cart/{userId}/clear
POST /api/orders
GET  /api/orders/{orderId}
GET  /api/orders/user/{userId}
POST /api/orders/{orderId}/cancel
POST /api/payments/create
POST /api/webhooks/payment/test
```
