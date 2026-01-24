import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getProducts, addToCart, isLoggedIn } from '../api/api'
import ProductCard from '../components/ProductCard'
import Loading from '../components/Loading'
import EmptyState from '../components/EmptyState'
import Toast from '../components/Toast'

function Products({ onCartUpdate }) {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    try {
      setLoading(true)
      const response = await getProducts()
      setProducts(response.data.content || [])
    } catch (err) {
      showToast('Failed to load products', 'error')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const showToast = (message, type = 'success') => {
    setToast({ message, type })
  }

  const handleAddToCart = async (productId, quantity) => {
    if (!isLoggedIn()) {
      navigate('/login')
      return
    }
    
    try {
      await addToCart(productId, quantity)
      showToast('Added to cart!')
      if (onCartUpdate) onCartUpdate()
    } catch (err) {
      showToast(err.response?.data?.error || err.response?.data?.message || 'Failed to add to cart', 'error')
    }
  }

  if (loading) {
    return <Loading text="Loading products" />
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h2 className="page-title">LABUBU Collection</h2>
          <p className="page-subtitle">Discover rare and exclusive collectibles</p>
        </div>
      </div>

      {products.length === 0 ? (
        <EmptyState
          icon="🧸"
          title="No Products Yet"
          message="Check back later for LABUBU drops"
        />
      ) : (
        <div className="product-grid">
          {products.map(product => (
            <ProductCard 
              key={product.id} 
              product={product} 
              onAddToCart={handleAddToCart}
            />
          ))}
        </div>
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

export default Products
