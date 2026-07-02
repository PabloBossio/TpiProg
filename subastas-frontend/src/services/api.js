import axios from 'axios'
import { emitToast } from '../lib/toastBus'

const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const enLogin = window.location.pathname === '/login'
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (!enLogin) {
        emitToast({ message: 'Tu sesión expiró o no tenés permisos. Volvé a iniciar sesión.', type: 'error' })
        window.location.href = '/login'
      }
    } else if (!error.response) {
      emitToast({ message: 'No se pudo conectar con el servidor. Verificá tu conexión.', type: 'error' })
    } else if (error.response.status >= 500) {
      emitToast({ message: 'Ocurrió un error inesperado en el servidor.', type: 'error' })
    }
    return Promise.reject(error)
  }
)

export const authService = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (nombreUsuario, email, password, rolesIds = []) =>
    api.post('/usuarios', { nombreUsuario, email, password, rolesIds }),
}

export const rolService = {
  listar: () => api.get('/roles'),
}

export const subastaService = {
  listar: (estado) => api.get('/subastas', { params: estado ? { estado } : {} }),
  obtener: (id) => api.get(`/subastas/${id}`),
  crear: (data, vendedorId) => api.post('/subastas', data, { params: { vendedorId } }),
  publicar: (subastaId) => api.put(`/subastas/${subastaId}/publicar`),
  pujar: (subastaId, oferenteId, monto) =>
    api.post(`/subastas/${subastaId}/pujar`, { monto }, { params: { oferenteId } }),
  cancelar: (subastaId, motivo) => api.put(`/subastas/${subastaId}/cancelar`, { motivo }),
}

export const usuarioService = {
  obtener: (id) => api.get(`/usuarios/${id}`),
  listarTodos: () => api.get('/usuarios'),
  subastas: (id) => api.get(`/usuarios/${id}/subastas`),
  pujas: (id) => api.get(`/usuarios/${id}/pujas`),
  bloquear: (id) => api.put(`/usuarios/${id}/bloquear`),
  desbloquear: (id) => api.put(`/usuarios/${id}/desbloquear`),
}

export const categoriaService = {
  listar: () => api.get('/categorias'),
  crear: (nombre) => api.post('/categorias', { nombre }),
}

export const reclamoService = {
  listar: () => api.get('/reclamos'),
  abrir: (subastaId, usuarioId, motivo, descripcion) =>
    api.post('/reclamos', { subastaId, motivo, descripcion }, { params: { usuarioId } }),
  resolver: (id, aceptado, comentario) =>
    api.put(`/reclamos/${id}/resolver`, { aceptado, comentario }),
}

export const notificacionService = {
  obtenerPorUsuario: (usuarioId) => api.get(`/notificaciones/usuario/${usuarioId}`),
  marcarLeida: (id) => api.put(`/notificaciones/${id}/leer`),
}

export default api
