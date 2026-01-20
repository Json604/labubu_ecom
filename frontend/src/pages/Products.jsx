import { useState, useEffect } from 'react'
import { getProducts, addToCart, USER_ID } from '../api/api'
import ProductCard from '../components/ProductCard'

function Products({ onCartUpdate }) {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    loadProducts()
  }, [])

  const loadProducts = async () => {
    try {
      setLoading(true)
      const response = await getProducts()
      setProducts(response.data)
    } catch (err) {
      setError('Failed to load products')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  const handleAddToCart = async (productId, quantity) => {
    try {
      await addToCart(USER_ID, productId, quantity)
      setMessage('Added to cart!')
      setTimeout(() => setMessage(null), 2000)
      if (onCartUpdate) onCartUpdate()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to add to cart')
      setTimeout(() => setError(null), 3000)
    }
  }

  if (loading) {
    return <div className="loading">Loading products...</div>
  }

  return (
    <div>
      <h2 className="page-title">LABUBU Collection</h2>
      
      {error && <div className="error">{error}</div>}
      {message && (
        <div style={{ 
          background: '#e8ffe8', 
          border: '4px solid #0a0', 
          padding: '15px', 
          marginBottom: '20px',
          fontWeight: '700'
        }}>
          {message}
        </div>
      )}

      {products.length === 0 ? (
        <div className="empty-state">
          <h3>No Products Yet</h3>
          <p>Check back later for LABUBU drops</p>
        </div>
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
    </div>
  )
}

export default Products
