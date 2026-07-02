import { useEffect, useState } from 'react'
import { subscribeToast } from '../lib/toastBus'

let idCounter = 0

export default function ToastContainer() {
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    return subscribeToast(({ message, type = 'error' }) => {
      const id = ++idCounter
      setToasts((t) => [...t, { id, message, type }])
      setTimeout(() => {
        setToasts((t) => t.filter((x) => x.id !== id))
      }, 5000)
    })
  }, [])

  if (toasts.length === 0) return null

  return (
    <div className="fixed top-4 right-4 z-[100] w-80 max-w-[90vw] space-y-2">
      {toasts.map((t) => (
        <div
          key={t.id}
          role="alert"
          className={`px-4 py-3 rounded-xl border text-sm shadow-2xl backdrop-blur-sm ${
            t.type === 'error'
              ? 'bg-red-950/90 border-red-500/30 text-red-300'
              : 'bg-emerald-950/90 border-emerald-500/30 text-emerald-300'
          }`}
        >
          {t.message}
        </div>
      ))}
    </div>
  )
}
