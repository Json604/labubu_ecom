import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register } from '../api/api'
import Toast from '../components/Toast'

function Register({ onLogin }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState(null)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await register(name, email, password)
      const { token, userId, role } = response.data
      
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({ userId, email, name, role }))
      
      if (onLogin) onLogin()
      navigate('/')
    } catch (err) {
      const errorData = err.response?.data
      let message = 'Registration failed'
      if (errorData?.message && typeof errorData.message === 'object') {
        message = Object.values(errorData.message).join(', ')
      } else if (errorData?.message) {
        message = errorData.message
      }
      setToast({ message, type: 'error' })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h2>Create Account</h2>
        </div>
        
        <div className="auth-body">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Name</label>
              <input
                type="text"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your name"
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </div>
            
            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                minLength={6}
              />
            </div>
            
            <button type="submit" className="btn btn-block btn-lg" disabled={loading}>
              {loading ? 'Creating account...' : 'Register'}
            </button>
          </form>
        </div>
        
        <div className="auth-footer">
          Already have an account? <Link to="/login">Login</Link>
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

export default Register
