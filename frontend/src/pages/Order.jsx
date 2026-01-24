import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getOrder, createPayment, cancelOrder, testWebhook } from '../api/api'
import Loading from '../components/Loading'
import EmptyState from '../components/EmptyState'
import Toast from '../components/Toast'

function Order() {
  const { orderId } = useParams()
  const navigate = useNavigate()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [processing, setProcessing] = useState(false)
  const [toast, setToast] = useState(null)

  useEffect(() => {
    loadOrder()
  }, [orderId])

  const loadOrder = async () => {
    try {
      setLoading(true)
      const response = await getOrder(orderId)
      setOrder(response.data)
    } catch (err) {
      showToast('Failed to load order', 'error')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const showToast = (message, type = 'success') => {
    setToast({ message, type })
  }

  const handlePayment = async () => {
    try {
      setProcessing(true)
      
      const response = await createPayment(orderId)
      const paymentData = response.data
      
      const options = {
        key: paymentData.razorpayKeyId,
        amount: paymentData.amount * 100,
        currency: 'INR',
        name: 'LABUBU STORE',
        description: `Order #${orderId}`,
        order_id: paymentData.razorpayOrderId,
        handler: async function (response) {
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
            showToast('Payment cancelled', 'error')
          }
        }
      }
      
      const razorpay = new window.Razorpay(options)
      razorpay.on('payment.failed', function (response) {
        showToast('Payment failed: ' + response.error.description, 'error')
        setProcessing(false)
      })
      razorpay.open()
      
    } catch (err) {
      showToast(err.response?.data?.error || err.response?.data?.message || 'Failed to create payment', 'error')
      setProcessing(false)
    }
  }

  const handleCancel = async () => {
    if (!confirm('Are you sure you want to cancel this order?')) return
    
    try {
      setProcessing(true)
      await cancelOrder(orderId)
      showToast('Order cancelled successfully')
      loadOrder()
    } catch (err) {
      showToast(err.response?.data?.error || err.response?.data?.message || 'Failed to cancel order', 'error')
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
    return <Loading text="Loading order" />
  }

  if (!order) {
    return (
      <EmptyState
        icon="📦"
        title="Order Not Found"
        message="This order doesn't exist or has been removed"
        actionText="Back to Products"
        onAction={() => navigate('/')}
      />
    )
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">Order Details</h2>
          <p className="page-subtitle">Order placed on {new Date(order.createdAt).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
          })}</p>
        </div>
      </div>

      <div className="order-card">
        <div className="order-header">
          <div>
            <h3 className="order-title">Order Summary</h3>
            <p className="order-id">{order.id}</p>
          </div>
          <span className={`order-status ${getStatusClass(order.status)}`}>
            {order.status}
          </span>
        </div>

        <div className="order-body">
          <div className="order-items">
            {order.items?.map((item, index) => (
              <div key={index} className="order-item">
                <div>
                  <span className="order-item-name">Product #{item.productId.slice(-6)}</span>
                  <span className="order-item-qty">× {item.quantity}</span>
                </div>
                <div>₹{(item.price * item.quantity).toLocaleString()}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="order-total">
          <span>Total Amount</span>
          <span className="order-total-amount">₹{order.totalAmount?.toLocaleString()}</span>
        </div>

        {order.payment && (
          <div className="order-body" style={{ borderTop: '3px solid #000' }}>
            <h4 style={{ marginBottom: '15px', textTransform: 'uppercase', fontSize: '0.9rem' }}>Payment Information</h4>
            <div className="order-item">
              <span>Status</span>
              <span style={{ fontWeight: '700' }}>{order.payment.status}</span>
            </div>
            <div className="order-item">
              <span>Amount</span>
              <span>₹{order.payment.amount?.toLocaleString()}</span>
            </div>
            {order.payment.razorpayOrderId && (
              <div className="order-item">
                <span>Razorpay ID</span>
                <span style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{order.payment.razorpayOrderId}</span>
              </div>
            )}
          </div>
        )}

        <div className="order-actions">
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

export default Order
