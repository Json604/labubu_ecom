import { useState, useEffect } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { testWebhook, getOrder } from '../api/api'

function PaymentResult() {
  const { orderId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [status, setStatus] = useState('processing')
  const [order, setOrder] = useState(null)
  const [error, setError] = useState(null)

  const payment = location.state?.payment

  useEffect(() => {
    if (payment?.razorpayOrderId) {
      processPayment()
    } else {
      // No payment info, just load order
      loadOrder()
    }
  }, [])

  const processPayment = async () => {
    try {
      // Simulate payment completion via test webhook
      await testWebhook(payment.razorpayOrderId, 'success')
      
      // Wait a moment then load updated order
      setTimeout(async () => {
        await loadOrder()
        setStatus('success')
      }, 1000)
    } catch (err) {
      setError('Payment processing failed')
      setStatus('failed')
      console.error(err)
    }
  }

  const loadOrder = async () => {
    try {
      const response = await getOrder(orderId)
      setOrder(response.data)
      if (response.data.status === 'PAID') {
        setStatus('success')
      }
    } catch (err) {
      console.error(err)
    }
  }

  if (status === 'processing') {
    return (
      <div className="result-box">
        <div className="result-icon">⏳</div>
        <h2>Processing Payment</h2>
        <p>Please wait while we confirm your payment...</p>
      </div>
    )
  }

  if (status === 'failed' || error) {
    return (
      <div className="result-box result-failed">
        <div className="result-icon">✗</div>
        <h2>Payment Failed</h2>
        <p>{error || 'Something went wrong with your payment'}</p>
        <button className="btn" onClick={() => navigate(`/order/${orderId}`)}>
          Try Again
        </button>
      </div>
    )
  }

  return (
    <div className="result-box result-success">
      <div className="result-icon">✓</div>
      <h2>Payment Successful!</h2>
      <p>Your LABUBU order has been confirmed</p>
      
      {order && (
        <div style={{ 
          textAlign: 'left', 
          border: '3px solid #000', 
          padding: '20px', 
          marginBottom: '20px',
          background: '#fff'
        }}>
          <p><strong>Order ID:</strong> {order.id}</p>
          <p><strong>Amount:</strong> ₹{order.totalAmount?.toLocaleString()}</p>
          <p><strong>Status:</strong> {order.status}</p>
        </div>
      )}
      
      <div style={{ display: 'flex', gap: '15px' }}>
        <button className="btn btn-secondary" onClick={() => navigate(`/order/${orderId}`)}>
          View Order
        </button>
        <button className="btn" onClick={() => navigate('/')}>
          Continue Shopping
        </button>
      </div>
    </div>
  )
}

export default PaymentResult
