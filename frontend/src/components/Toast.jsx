import { useEffect } from 'react'

function Toast({ message, type = 'success', onClose, duration = 3000 }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      if (onClose) onClose()
    }, duration)
    return () => clearTimeout(timer)
  }, [duration, onClose])

  if (!message) return null

  return (
    <div className={`toast toast-${type}`}>
      {message}
    </div>
  )
}

export default Toast
