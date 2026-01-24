import { useState, useEffect } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { testWebhook, getOrder } from '../api/api'
import Loading from '../components/Loading'

function PaymentResult() {
  const { orderId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [status, setStatus] = useState('processing')
  const [order, setOrder] = useState(null)

  const payment = location.state?.payment

  useEffect(() => {
    if (payment?.razorpayOrderId) {
      processPayment()
    } else {
      loadOrder()
    }
  }, [])

  const processPayment = async () => {
    try {
      await testWebhook(payment.razorpayOrderId, 'success')
      
      setTimeout(async () => {
        await loadOrder()
        setStatus('success')
      }, 1000)
    } catch (err) {
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
      <div className="payment-card">
        <div className="payment-icon">⏳</div>
        <h2 className="payment-title">Processing Payment</h2>
        <p className="payment-message">Please wait while we confirm your payment...</p>
        <Loading text="Verifying" />
      </div>
    )
  }

  if (status === 'failed') {
    return (
      <div className="payment-card payment-failed">
        <div className="payment-icon">✗</div>
        <h2 className="payment-title">Payment Failed</h2>
        <p className="payment-message">Something went wrong with your payment</p>
        <button className="btn" onClick={() => navigate(`/order/${orderId}`)}>
          Try Again
        </button>
      </div>
    )
  }

  return (
    <div className="payment-card payment-success">
      <div className="payment-icon">✓</div>
      <h2 className="payment-title">Payment Successful!</h2>
      <p className="payment-message">Your LABUBU order has been confirmed</p>
      
      {order && (
        <>
          <div className="payment-amount">
            ₹{order.totalAmount?.toLocaleString()}
          </div>
          
          <div className="payment-details">
            <p><strong>Order ID:</strong> {order.id}</p>
            <p><strong>Status:</strong> {order.status}</p>
            <p><strong>Items:</strong> {order.items?.length || 0} product(s)</p>
          </div>
        </>
      )}
      
      <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
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
