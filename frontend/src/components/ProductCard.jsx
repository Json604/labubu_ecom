import { useState } from 'react'

function ProductCard({ product, onAddToCart }) {
  const [quantity, setQuantity] = useState(1)
  const [adding, setAdding] = useState(false)

  const handleAdd = async () => {
    setAdding(true)
    await onAddToCart(product.id, quantity)
    setAdding(false)
    setQuantity(1)
  }

  const isLowStock = product.stock > 0 && product.stock < 5
  const isOutOfStock = product.stock === 0

  return (
    <div className="product-card">
      
      <div className="product-content">
        <h3>{product.name}</h3>
        {product.edition && (
          <span className="product-edition">{product.edition}</span>
        )}
        <div className="product-price">₹{product.price?.toLocaleString()}</div>
        <div className={`product-stock ${isLowStock ? 'stock-low' : ''} ${isOutOfStock ? 'stock-out' : ''}`}>
          {isOutOfStock ? 'OUT OF STOCK' : `${product.stock} in stock`}
        </div>
        
        {!isOutOfStock && (
          <div className="product-actions">
            <div className="quantity-selector">
              <button 
                className="quantity-btn"
                onClick={() => setQuantity(q => Math.max(1, q - 1))}
              >
                −
              </button>
              <span className="quantity-value">{quantity}</span>
              <button 
                className="quantity-btn"
                onClick={() => setQuantity(q => Math.min(product.stock, q + 1))}
              >
                +
              </button>
            </div>
            <button 
              className="btn btn-block" 
              onClick={handleAdd}
              disabled={adding}
            >
              {adding ? 'Adding...' : 'Add to Cart'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default ProductCard
