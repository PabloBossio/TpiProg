import { createContext, useContext, useState, useCallback } from 'react'
import { authService } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  })

  const login = useCallback(async (username, password) => {
    const { data } = await authService.login(username, password)
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
      roles: data.roles ?? [],
    }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
    return userData
  }, [])

  const register = useCallback(async (nombreUsuario, email, password, rolesIds = []) => {
    const { data } = await authService.register(nombreUsuario, email, password, rolesIds)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }, [])

  const isAdmin = user?.roles?.includes('ROLE_ADMIN') ?? false
  const isSeller = user?.roles?.includes('ROLE_SELLER') ?? false

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: !!user, isAdmin, isSeller }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
