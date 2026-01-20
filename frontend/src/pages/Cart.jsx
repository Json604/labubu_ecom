import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCart, clearCart, createOrder, createPayment, testWebhook, USER_ID } from '../api/api'

function Cart({ onCartUpdate }) {
  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [processing, setProcessing] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    loadCart()
  }, [])

  const loadCart = async () => {
    try {
      setLoading(true)
      const response = await getCart(USER_ID)
      setCartItems(response.data)
    } catch (err) {
      setError('Failed to load cart')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleClearCart = async () => {
    try {
      await clearCart(USER_ID)
      setCartItems([])
      if (onCartUpdate) onCartUpdate()
    } catch (err) {
      setError('Failed to clear cart')
    }
  }

  const handlePayNow = async () => {
    try {
      setProcessing(true)
      setError(null)
      
      // Step 1: Create order from cart
      const orderResponse = await createOrder(USER_ID)
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
            // Order created but payment cancelled - redirect to order page
            navigate(`/order/${order.id}`)
          }
        }
      }
      
      const razorpay = new window.Razorpay(options)
      razorpay.on('payment.failed', function (response) {
        setError('Payment failed: ' + response.error.description)
        setProcessing(false)
        navigate(`/order/${order.id}`)
      })
      razorpay.open()
      
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to process payment')
      setProcessing(false)
    }
  }

  const calculateTotal = () => {
    return cartItems.reduce((total, item) => {
      const price = item.product?.price || 0
      return total + (price * item.quantity)
    }, 0)
  }

  if (loading) {
    return <div className="loading">Loading cart...</div>
  }

  return (
    <div>
      <h2 className="page-title">Your Cart</h2>
      
      {error && <div className="error">{error}</div>}

      {cartItems.length === 0 ? (
        <div className="empty-state">
          <h3>Cart is Empty</h3>
          <p>Add some LABUBU to your collection</p>
          <button className="btn" onClick={() => navigate('/')} style={{ width: 'auto', marginTop: '20px' }}>
            Browse Products
          </button>
        </div>
      ) : (
        <>
          {cartItems.map(item => (
            <div key={item.id} className="cart-item">
              <div className="cart-item-info">
                <h3>{item.product?.name || 'Unknown Product'}</h3>
                <p>Qty: {item.quantity} × ₹{item.product?.price?.toLocaleString()}</p>
              </div>
              <div className="cart-item-price">
                ₹{((item.product?.price || 0) * item.quantity).toLocaleString()}
              </div>
            </div>
          ))}

          <div className="cart-total">
            <h3>Total</h3>
            <div className="cart-total-amount">₹{calculateTotal().toLocaleString()}</div>
          </div>

          <div className="cart-actions">
            <button className="btn btn-secondary" onClick={handleClearCart} disabled={processing}>
              Clear Cart
            </button>
            <button 
              className="btn" 
              onClick={handlePayNow}
              disabled={processing}
            >
              {processing ? 'Processing...' : 'Pay Now'}
            </button>
          </div>
        </>
      )}
    </div>
  )
}

export default Cart
