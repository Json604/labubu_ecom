import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

export const USER_ID = 'user123'

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Products
export const getProducts = () => api.get('/products')

export const searchProducts = (query) => api.get(`/products/search?q=${query}`)

// Cart
export const getCart = (userId) => api.get(`/cart/${userId}`)

export const addToCart = (userId, productId, quantity) => 
  api.post('/cart/add', { userId, productId, quantity })

export const clearCart = (userId) => api.delete(`/cart/${userId}/clear`)

// Orders
export const createOrder = (userId) => api.post('/orders', { userId })

export const getOrder = (orderId) => api.get(`/orders/${orderId}`)

export const getUserOrders = (userId) => api.get(`/orders/user/${userId}`)

export const cancelOrder = (orderId) => api.post(`/orders/${orderId}/cancel`)

// Payments
export const createPayment = (orderId) => api.post('/payments/create', { orderId })

export const testWebhook = (razorpayOrderId, status = 'success') => 
  api.post('/webhooks/payment/test', { 
    razorpay_order_id: razorpayOrderId, 
    status 
  })

export default api
