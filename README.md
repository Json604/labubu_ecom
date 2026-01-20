# LABUBU Store - E-Commerce Application

A minimal e-commerce system for a LABUBU collectible toy store with Spring Boot backend and React frontend.

## Project Structure

```
├── backend/     → Spring Boot REST API
├── frontend/    → React + Vite UI
└── README.md
```

## Quick Start

### 1. Start Backend

```bash
cd backend
export MONGODB_URI="your_mongodb_uri"
export MONGODB_DATABASE="labubu_store"
export RAZORPAY_KEY_ID="your_razorpay_key"
export RAZORPAY_KEY_SECRET="your_razorpay_secret"
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

### 2. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

## Documentation

- [Backend README](./backend/README.md) - API details, MongoDB setup, Razorpay integration
- [Frontend README](./frontend/README.md) - React app setup, user flow, pages

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot, MongoDB |
| Frontend | React 18, Vite, Axios |
| Payments | Razorpay |

## Features

- Product catalog with LABUBU variants
- Shopping cart
- Order management
- Real Razorpay payment integration
- Order cancellation with stock restore
