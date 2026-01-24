import { NavLink, useNavigate } from 'react-router-dom'
import { isLoggedIn, getUser, logout } from '../api/api'

function Layout({ children, cartCount, onLogout }) {
  const navigate = useNavigate()
  const user = getUser()

  const handleLogout = () => {
    logout()
    if (onLogout) onLogout()
    navigate('/')
  }

  return (
    <div className="app-container">
      <header className="header">
        <div className="container">
          <div className="header-inner">
            <div className="logo" onClick={() => navigate('/')}>
              <h1>LABUBU STORE</h1>
              <p className="logo-subtitle">Collectible toys for serious collectors</p>
            </div>

            <nav className="nav">
              <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                Products
              </NavLink>

              {isLoggedIn() ? (
                <>
                  <NavLink to="/cart" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                    Cart
                    {cartCount > 0 && <span className="badge">{cartCount}</span>}
                  </NavLink>
                  <NavLink to="/orders" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                    Orders
                  </NavLink>
                  <button onClick={handleLogout} className="nav-link logout-btn">
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <NavLink to="/login" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                    Login
                  </NavLink>
                  <NavLink to="/register" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                    Register
                  </NavLink>
                </>
              )}
            </nav>
          </div>
        </div>
      </header>

      <main className="main-content">
        <div className="container">
          {children}
        </div>
      </main>

      <footer className="footer">
        <div className="container">
          <div className="footer-content">
            <p className="footer-text">© {new Date().getFullYear()} LABUBU Store. All rights reserved.</p>
            <div className="footer-links">
              <a href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noopener noreferrer">
                API Docs
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default Layout
