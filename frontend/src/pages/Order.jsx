import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getOrder, createPayment, cancelOrder, testWebhook } from '../api/api'

function Order() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [processing, setProcessing] = useState(false)

  useEffect(() => {
    loadOrder()
  }, [orderId])

  const loadOrder = async () => {
    try {
      setLoading(true)
      const response = await getOrder(orderId)
      setOrder(response.data)
    } catch (err) {
      setError('Failed to load order')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handlePayment = async () => {
    try {
      setProcessing(true)
      setError(null)
      
      // Create Razorpay order via backend
      const response = await createPayment(orderId)
      const paymentData = response.data
      
      // Open Razorpay checkout
      const options = {
        key: paymentData.razorpayKeyId,
        amount: paymentData.amount * 100, // Razorpay expects paise
        currency: 'INR',
        name: 'LABUBU STORE',
        description: `Order #${orderId}`,
        order_id: paymentData.razorpayOrderId,
        handler: async function (response) {
          // Payment successful - call test webhook to update backend
          // In production, Razorpay sends webhook automatically
          try {
            await testWebhook(paymentData.razorpayOrderId, 'success')
            navigate(`/payment/${orderId}`, { 
              state: { 
                payment: paymentData,
                razorpayResponse: response,
                success: true
              } 
            })
          } catch (err) {
            console.error('Webhook failed:', err)
            navigate(`/payment/${orderId}`, { 
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
            setError('Payment cancelled')
          }
        }
      }
      
      const razorpay = new window.Razorpay(options)
      razorpay.on('payment.failed', function (response) {
        setError('Payment failed: ' + response.error.description)
        setProcessing(false)
      })
      razorpay.open()
      
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create payment')
      setProcessing(false)
    }
  }

  const handleCancel = async () => {
    if (!confirm('Are you sure you want to cancel this order?')) return
    
    try {
      setProcessing(true)
      await cancelOrder(orderId)
      loadOrder()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to cancel order')
    } finally {
      setProcessing(false)
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
    return <div className="loading">Loading order...</div>
  }

  if (!order) {
    return (
      <div className="empty-state">
        <h3>Order Not Found</h3>
        <button className="btn" onClick={() => navigate('/')} style={{ width: 'auto', marginTop: '20px' }}>
          Back to Products
        </button>
      </div>
    )
  }

  return (
    <div>
      <h2 className="page-title">Order Details</h2>
      
      {error && <div className="error">{error}</div>}

      <div className="order-box">
        <div className="order-header">
          <div>
            <h3>Order</h3>
            <span className="order-id">{order.id}</span>
          </div>
          <span className={`order-status ${getStatusClass(order.status)}`}>
            {order.status}
          </span>
        </div>

        <div className="order-items">
          {order.items?.map((item, index) => (
            <div key={index} className="order-item">
              <div>
                <strong>Product ID:</strong> {item.productId}
                <br />
                <span style={{ color: '#666' }}>Qty: {item.quantity}</span>
              </div>
              <div>₹{(item.price * item.quantity).toLocaleString()}</div>
            </div>
          ))}
        </div>

        <div className="order-total">
          <span>Total</span>
          <span>₹{order.totalAmount?.toLocaleString()}</span>
        </div>
      </div>

      {order.payment && (
        <div className="order-box">
          <h3 style={{ marginBottom: '15px' }}>Payment Info</h3>
          <p><strong>Status:</strong> {order.payment.status}</p>
          <p><strong>Amount:</strong> ₹{order.payment.amount?.toLocaleString()}</p>
          {order.payment.razorpayOrderId && (
            <p><strong>Razorpay ID:</strong> {order.payment.razorpayOrderId}</p>
          )}
        </div>
      )}

      <div className="cart-actions">
        {order.status === 'CREATED' && (
          <>
            <button 
              className="btn btn-danger" 
              onClick={handleCancel}
              disabled={processing}
            >
              Cancel Order
            </button>
            <button 
              className="btn" 
              onClick={handlePayment}
              disabled={processing}
            >
              {processing ? 'Processing...' : 'Pay Now'}
            </button>
          </>
        )}
        
        {order.status === 'PAID' && (
          <>
            <button 
              className="btn btn-danger" 
              onClick={handleCancel}
              disabled={processing}
            >
              Cancel & Refund
            </button>
            <button className="btn btn-success" disabled>
              ✓ Payment Complete
            </button>
          </>
        )}
        
        {order.status === 'CANCELLED' && (
          <button className="btn" onClick={() => navigate('/')}>
            Continue Shopping
          </button>
        )}
      </div>
    </div>
  )
}

export default Order
