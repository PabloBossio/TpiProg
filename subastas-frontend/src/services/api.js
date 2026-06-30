import axios from 'axios'

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
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const authService = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (nombreUsuario, email, password) =>
    api.post('/usuarios', { nombreUsuario, email, password, rolesIds: [] }),
}

export const subastaService = {
  listar: (estado) => api.get('/subastas', { params: estado ? { estado } : {} }),
  obtener: (id) => api.get(`/subastas/${id}`),
  crear: (data, vendedorId) => api.post('/subastas', data, { params: { vendedorId } }),
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
}

export const reclamoService = {
  listar: () => api.get('/reclamos'),
  abrir: (subastaId, usuarioId, motivo, descripcion) =>
    api.post('/reclamos', { subastaId, motivo, descripcion }, { params: { usuarioId } }),
  resolver: (id, resolucion, estadoFinal) =>
    api.put(`/reclamos/${id}/resolver`, { resolucion, estadoFinal }),
}

export const notificacionService = {
  obtenerPorUsuario: (usuarioId) => api.get(`/notificaciones/usuario/${usuarioId}`),
  marcarLeida: (id) => api.put(`/notificaciones/${id}/leer`),
}

export default api
