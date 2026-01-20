import { useState, useEffect } from 'react'
import { Routes, Route, NavLink, useNavigate } from 'react-router-dom'
import Products from './pages/Products'
import Cart from './pages/Cart'
import Order from './pages/Order'
import PaymentResult from './pages/PaymentResult'
import { getCart, getUserOrders, USER_ID } from './api/api'

function App() {
  const [cartCount, setCartCount] = useState(0)
  const navigate = useNavigate()

  useEffect(() => {
    loadCartCount()
  }, [])

  const loadCartCount = async () => {
    try {
      const response = await getCart(USER_ID)
      const count = response.data.reduce((sum, item) => sum + item.quantity, 0)
      setCartCount(count)
    } catch (err) {
      console.error('Failed to load cart count')
    }
  }

  return (
    <div className="container">
      <header className="header">
        <h1 onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          LABUBU STORE
        </h1>
        <p className="header-subtitle">Collectible toys for serious collectors</p>
        
        <nav className="nav">
          <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Products
          </NavLink>
          <NavLink to="/cart" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Cart {cartCount > 0 && `(${cartCount})`}
          </NavLink>
          <NavLink to="/orders" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Orders
          </NavLink>
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Products onCartUpdate={loadCartCount} />} />
          <Route path="/cart" element={<Cart onCartUpdate={loadCartCount} />} />
          <Route path="/orders" element={<OrdersList />} />
          <Route path="/order/:orderId" element={<Order />} />
          <Route path="/payment/:orderId" element={<PaymentResult />} />
        </Routes>
      </main>
    </div>
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
      const response = await getUserOrders(USER_ID)
      setOrders(response.data)
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
    return <div className="loading">Loading orders...</div>
  }

  return (
    <div>
      <h2 className="page-title">Your Orders</h2>
      
      {orders.length === 0 ? (
        <div className="empty-state">
          <h3>No Orders Yet</h3>
          <p>Start shopping to see your orders here</p>
          <button className="btn" onClick={() => navigate('/')} style={{ width: 'auto', marginTop: '20px' }}>
            Browse Products
          </button>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <div 
              key={order.id} 
              className="order-list-item"
              onClick={() => navigate(`/order/${order.id}`)}
            >
              <div>
                <strong style={{ fontSize: '0.85rem' }}>{order.id}</strong>
                <p style={{ color: '#666', fontSize: '0.85rem', marginTop: '5px' }}>
                  {new Date(order.createdAt).toLocaleDateString()}
                </p>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <span style={{ fontWeight: '700', fontSize: '1.1rem' }}>
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
