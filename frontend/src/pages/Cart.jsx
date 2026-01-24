import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCart, clearCart, createOrder, createPayment, testWebhook } from '../api/api'
import Loading from '../components/Loading'
import EmptyState from '../components/EmptyState'
import Toast from '../components/Toast'

function Cart({ onCartUpdate }) {
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [toast, setToast] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadCart()
  }, [])

  const loadCart = async () => {
    try {
      setLoading(true)
      const response = await getCart()
      setCartItems(response.data)
    } catch (err) {
      showToast('Failed to load cart', 'error')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const showToast = (message, type = 'success') => {
    setToast({ message, type })
  }

  const handleClearCart = async () => {
    try {
      await clearCart()
      setCartItems([])
      showToast('Cart cleared')
      if (onCartUpdate) onCartUpdate()
    } catch (err) {
      showToast('Failed to clear cart', 'error')
    }
  }

  const handlePayNow = async () => {
    try {
      setProcessing(true)
      
      // Step 1: Create order from cart
      const orderResponse = await createOrder()
      const order = orderResponse.data
      if (onCartUpdate) onCartUpdate()
      
      // Step 2: Create Razorpay payment
      const paymentResponse = await createPayment(order.id)
      const paymentData = paymentResponse.data
      
      // Step 3: Open Razorpay checkout
      const options = {
        key: paymentData.razorpayKeyId,
        amount: paymentData.amount * 100,
        currency: 'INR',
        name: 'LABUBU STORE',
        description: `Order #${order.id}`,
        order_id: paymentData.razorpayOrderId,
        handler: async function (response) {
          try {
            await testWebhook(paymentData.razorpayOrderId, 'success')
            navigate(`/payment/${order.id}`, { 
              state: { 
                payment: paymentData,
                razorpayResponse: response,
                success: true
              } 
            })
          } catch (err) {
            console.error('Webhook failed:', err)
            navigate(`/payment/${order.id}`, { 
              state: { payment: paymentData, success: true } 
            })
          }
        },
        prefill: {
          name: 'LABUBU Collector',
          email: 'collector@labubu.store',
          contact: '9999999999'
        },
        theme: {
          color: '#000000'
        },
        modal: {
          ondismiss: function() {
            setProcessing(false)
            navigate(`/order/${order.id}`)
          }
        }
      }
      
      const razorpay = new window.Razorpay(options)
      razorpay.on('payment.failed', function (response) {
        showToast('Payment failed: ' + response.error.description, 'error')
        setProcessing(false)
        navigate(`/order/${order.id}`)
      })
      razorpay.open()
      
    } catch (err) {
      showToast(err.response?.data?.error || err.response?.data?.message || 'Failed to process payment', 'error')
      setProcessing(false)
    }
  }

  const calculateTotal = () => {
    return cartItems.reduce((total, item) => {
      const price = item.product?.price || 0
      return total + (price * item.quantity)
    }, 0)
  }

  const itemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0)

  if (loading) {
    return <Loading text="Loading cart" />
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Your Cart</h2>
          <p className="page-subtitle">{itemCount} {itemCount === 1 ? 'item' : 'items'} in your cart</p>
        </div>
      </div>

      {cartItems.length === 0 ? (
        <EmptyState
          icon="🛒"
          title="Cart is Empty"
          message="Add some LABUBU to your collection"
          actionText="Browse Products"
          onAction={() => navigate('/')}
        />
      ) : (
        <>
          {cartItems.map(item => (
            <div key={item.id} className="cart-item">
              <div className="cart-item-info">
                <h3>{item.product?.name || 'Unknown Product'}</h3>
                <p className="cart-item-details">
                  Qty: {item.quantity} × ₹{item.product?.price?.toLocaleString()}
                </p>
              </div>
              <div className="cart-item-price">
                ₹{((item.product?.price || 0) * item.quantity).toLocaleString()}
              </div>
            </div>
          ))}

          <div className="cart-summary">
            <div className="cart-summary-row">
              <span className="cart-summary-label">Subtotal</span>
              <span className="cart-summary-value">₹{calculateTotal().toLocaleString()}</span>
            </div>
            <div className="cart-summary-row">
              <span className="cart-summary-label">Shipping</span>
              <span className="cart-summary-value">FREE</span>
            </div>
            <div className="cart-summary-row">
              <span className="cart-summary-label">Total</span>
              <span className="cart-total-value">₹{calculateTotal().toLocaleString()}</span>
            </div>
          </div>

          <div className="cart-actions">
            <button className="btn btn-secondary" onClick={handleClearCart} disabled={processing}>
              Clear Cart
            </button>
            <button 
              className="btn btn-lg" 
              onClick={handlePayNow}
              disabled={processing}
            >
              {processing ? 'Processing...' : 'Pay Now'}
            </button>
          </div>
        </>
      )}

      {toast && (
        <Toast 
          message={toast.message} 
          type={toast.type} 
          onClose={() => setToast(null)} 
        />
      )}
    </div>
  )
}

export default Cart
