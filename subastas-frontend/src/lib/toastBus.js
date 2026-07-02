const listeners = new Set()

export function emitToast(toast) {
  listeners.forEach((fn) => fn(toast))
}

export function subscribeToast(fn) {
  listeners.add(fn)
  return () => listeners.delete(fn)
}
