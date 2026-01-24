import { useState, useEffect } from 'react'
import { Routes, Route, useNavigate, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Products from './pages/Products'
import Cart from './pages/Cart'
import Order from './pages/Order'
import PaymentResult from './pages/PaymentResult'
import Login from './pages/Login'
import Register from './pages/Register'
import Loading from './components/Loading'
import EmptyState from './components/EmptyState'
import { getCart, getOrders, isLoggedIn, getUser, logout } from './api/api'

function App() {
  const [cartCount, setCartCount] = useState(0)
  const [user, setUser] = useState(getUser())
  const navigate = useNavigate()

  useEffect(() => {
    if (isLoggedIn()) {
      loadCartCount()
    }
  }, [user])

  const loadCartCount = async () => {
    try {
      const response = await getCart()
      const count = response.data.reduce((sum, item) => sum + item.quantity, 0)
      setCartCount(count)
    } catch (err) {
      console.error('Failed to load cart count')
    }
  }

  const handleLogin = () => {
    setUser(getUser())
  }

  const handleLogout = () => {
    logout()
    setUser(null)
    setCartCount(0)
    navigate('/')
  }

  return (
    <Layout cartCount={cartCount} onLogout={handleLogout}>
      <Routes>
        <Route path="/" element={<Products onCartUpdate={loadCartCount} />} />
        <Route path="/login" element={<Login onLogin={handleLogin} />} />
        <Route path="/register" element={<Register onLogin={handleLogin} />} />
        
        {/* Protected routes */}
        <Route path="/cart" element={
          isLoggedIn() ? <Cart onCartUpdate={loadCartCount} /> : <Navigate to="/login" />
        } />
        <Route path="/orders" element={
          isLoggedIn() ? <OrdersList /> : <Navigate to="/login" />
        } />
        <Route path="/order/:orderId" element={
          isLoggedIn() ? <Order /> : <Navigate to="/login" />
        } />
        <Route path="/payment/:orderId" element={
          isLoggedIn() ? <PaymentResult /> : <Navigate to="/login" />
        } />
      </Routes>
    </Layout>
  )
}

function OrdersList() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    loadOrders()
  }, [])

  const loadOrders = async () => {
    try {
      const response = await getOrders()
      setOrders(response.data.content || [])
    } catch (err) {
      console.error('Failed to load orders')
    } finally {
      setLoading(false)
    }
  }

  const getStatusClass = (status) => {
    switch (status) {
      case 'PAID': return 'status-paid'
      case 'CANCELLED': return 'status-cancelled'
      default: return 'status-created'
    }
  }

  if (loading) {
    return <Loading text="Loading orders" />
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Your Orders</h2>
          <p className="page-subtitle">Track your LABUBU collection journey</p>
        </div>
      </div>
      
      {orders.length === 0 ? (
        <EmptyState
          icon="📦"
          title="No Orders Yet"
          message="Start shopping to see your orders here"
          actionText="Browse Products"
          onAction={() => navigate('/')}
        />
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <div 
              key={order.id} 
              className="order-list-item"
              onClick={() => navigate(`/order/${order.id}`)}
            >
              <div className="order-list-info">
                <h4>{order.id}</h4>
                <p className="order-list-date">
                  {new Date(order.createdAt).toLocaleDateString('en-IN', {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric'
                  })}
                </p>
              </div>
              <div className="order-list-right">
                <span className="order-list-amount">
                  ₹{order.totalAmount?.toLocaleString()}
                </span>
                <span className={`order-status ${getStatusClass(order.status)}`}>
                  {order.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default App
