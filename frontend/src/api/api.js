import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add token to requests if available
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401 errors
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Don't redirect if already on login/register page
      const isAuthPage = window.location.pathname === '/login' || window.location.pathname === '/register'
      if (!isAuthPage) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Auth
export const register = (name, email, password) => 
  api.post('/auth/register', { name, email, password })

export const login = (email, password) => 
  api.post('/auth/login', { email, password })

export const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export const getUser = () => {
  const user = localStorage.getItem('user')
  return user ? JSON.parse(user) : null
}

export const isLoggedIn = () => !!localStorage.getItem('token')

// Products
export const getProducts = (page = 0, size = 10, filters = {}) => {
  const params = new URLSearchParams({ page, size, ...filters })
  return api.get(`/products?${params}`)
}

export const searchProducts = (query, page = 0, size = 10) => 
  api.get(`/products/search?q=${query}&page=${page}&size=${size}`)

export const getProduct = (id) => api.get(`/products/${id}`)

// Cart
export const getCart = () => api.get('/cart')

export const addToCart = (productId, quantity) => 
  api.post('/cart/add', { productId, quantity })

export const clearCart = () => api.delete('/cart/clear')

// Orders
export const createOrder = () => api.post('/orders')

export const getOrders = (page = 0, size = 10) => 
  api.get(`/orders?page=${page}&size=${size}`)

export const getOrder = (orderId) => api.get(`/orders/${orderId}`)

export const cancelOrder = (orderId) => api.post(`/orders/${orderId}/cancel`)

// Payments
export const createPayment = (orderId) => api.post('/payments/create', { orderId })

export const testWebhook = (razorpayOrderId, status = 'success') => 
  api.post('/webhooks/payment/test', { 
    razorpay_order_id: razorpayOrderId, 
    status 
  })

export default api
