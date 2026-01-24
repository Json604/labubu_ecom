function EmptyState({ icon = '📦', title, message, actionText, onAction }) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">{icon}</div>
      <h3>{title}</h3>
      <p>{message}</p>
      {actionText && onAction && (
        <button className="btn" onClick={onAction}>
          {actionText}
        </button>
      )}
    </div>
  )
}

export default EmptyState
