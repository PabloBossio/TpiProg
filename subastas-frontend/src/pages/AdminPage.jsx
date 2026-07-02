import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { usuarioService, reclamoService } from '../services/api'
import { parseServerDate } from '../lib/dates'

function TabButton({ active, onClick, children }) {
  return (
    <button
      onClick={onClick}
      className={`px-5 py-2.5 text-sm font-medium rounded-full transition-all duration-200 cursor-pointer ${
        active
          ? 'bg-amber-500 text-white shadow-lg shadow-amber-500/20'
          : 'text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-700/50'
      }`}
    >
      {children}
    </button>
  )
}

function UsuariosPanel() {
  const [usuarios, setUsuarios] = useState([])
  const [loading, setLoading] = useState(true)
  const [accionId, setAccionId] = useState(null)

  const cargar = useCallback(() => {
    setLoading(true)
    usuarioService.listarTodos()
      .then(({ data }) => setUsuarios(data))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { cargar() }, [cargar])

  const toggleBloqueo = async (usuario) => {
    setAccionId(usuario.id)
    try {
      const fn = usuario.estaBloqueado ? usuarioService.desbloquear : usuarioService.bloquear
      const { data } = await fn(usuario.id)
      setUsuarios(prev => prev.map(u => u.id === data.id ? data : u))
    } finally {
      setAccionId(null)
    }
  }

  if (loading) return (
    <div className="flex justify-center py-16">
      <div className="w-8 h-8 border-2 border-amber-500/30 border-t-amber-500 rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="space-y-2">
      {usuarios.map(u => (
        <div
          key={u.id}
          className={`flex items-center gap-4 rounded-2xl border p-4 transition-colors ${
            u.estaBloqueado
              ? 'bg-red-500/8 border-red-500/20'
              : 'bg-slate-900/60 border-slate-800/80'
          }`}
        >
          <div className={`w-11 h-11 rounded-xl flex items-center justify-center font-bold text-sm shrink-0 ${
            u.estaBloqueado
              ? 'bg-red-500/15 text-red-400 border border-red-500/20'
              : 'bg-indigo-500/15 text-indigo-400 border border-indigo-500/20'
          }`}>
            {u.nombreUsuario[0].toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-semibold text-slate-200 text-sm">{u.nombreUsuario}</p>
            <p className="text-xs text-slate-500">{u.email}</p>
            <div className="flex gap-1.5 mt-1.5 flex-wrap">
              {u.roles.map(r => (
                <span key={r} className="text-xs bg-slate-800/80 text-slate-500 px-2 py-0.5 rounded-full border border-slate-700/50">{r}</span>
              ))}
              {u.estaBloqueado && (
                <span className="text-xs bg-red-500/15 text-red-400 px-2 py-0.5 rounded-full border border-red-500/20 font-medium">BLOQUEADO</span>
              )}
            </div>
          </div>
          <button
            onClick={() => toggleBloqueo(u)}
            disabled={accionId === u.id}
            className={`shrink-0 text-sm font-medium px-4 py-2 rounded-xl transition-all duration-200 cursor-pointer disabled:opacity-40 border ${
              u.estaBloqueado
                ? 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/20 hover:border-emerald-500/40'
                : 'bg-red-500/10 hover:bg-red-500/20 text-red-400 border-red-500/20 hover:border-red-500/40'
            }`}
          >
            {accionId === u.id ? (
              <span className="flex items-center gap-1.5">
                <span className="w-3.5 h-3.5 border-2 border-current/30 border-t-current rounded-full animate-spin" />
              </span>
            ) : u.estaBloqueado ? 'Desbloquear' : 'Bloquear'}
          </button>
        </div>
      ))}
    </div>
  )
}

function DisputasPanel() {
  const [reclamos, setReclamos] = useState([])
  const [loading, setLoading] = useState(true)
  const [resolviendo, setResolviendo] = useState(null)
  const [comentario, setComentario] = useState('')
  const [accionLoading, setAccionLoading] = useState(false)
  const [error, setError] = useState('')

  const cargar = useCallback(() => {
    setLoading(true)
    reclamoService.listar()
      .then(({ data }) => setReclamos(data))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { cargar() }, [cargar])

  const handleResolver = async (id, aceptado) => {
    setAccionLoading(true); setError('')
    try {
      await reclamoService.resolver(id, aceptado, comentario.trim() || null)
      setResolviendo(null)
      setComentario('')
      cargar()
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Error al resolver el reclamo.'
      setError(typeof msg === 'string' ? msg : 'Error al resolver el reclamo.')
    } finally {
      setAccionLoading(false)
    }
  }

  if (loading) return (
    <div className="flex justify-center py-16">
      <div className="w-8 h-8 border-2 border-amber-500/30 border-t-amber-500 rounded-full animate-spin" />
    </div>
  )

  if (reclamos.length === 0) return (
    <div className="text-center py-20">
      <div className="w-16 h-16 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center justify-center mx-auto mb-5">
        <svg className="w-8 h-8 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p className="text-slate-400 font-medium">No hay disputas pendientes.</p>
    </div>
  )

  return (
    <div className="space-y-4">
      {reclamos.map(r => (
        <div key={r.id} className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-5">
          <div className="flex items-start justify-between gap-3 mb-4">
            <div>
              <Link
                to={`/subastas/${r.subastaId}`}
                className="font-semibold text-indigo-400 hover:text-indigo-300 text-sm transition-colors"
              >
                Subasta #{r.subastaId}
              </Link>
              <p className="text-xs text-slate-500 mt-0.5">
                Abierto por <span className="text-slate-400 font-medium">{r.usuarioDemandanteNombre}</span>
                {' · '}{parseServerDate(r.fechaCreacion).toLocaleString('es-AR')}
              </p>
            </div>
            {r.resultado === 'ACEPTADO' ? (
              <span className="shrink-0 text-xs bg-emerald-500/15 text-emerald-400 px-2.5 py-1 rounded-full border border-emerald-500/20 font-medium">Aceptado</span>
            ) : r.resultado === 'RECHAZADO' ? (
              <span className="shrink-0 text-xs bg-slate-500/15 text-slate-400 px-2.5 py-1 rounded-full border border-slate-500/20 font-medium">Rechazado</span>
            ) : (
              <span className="shrink-0 text-xs bg-orange-500/15 text-orange-400 px-2.5 py-1 rounded-full border border-orange-500/20 font-medium">Pendiente</span>
            )}
          </div>

          <p className="text-sm font-semibold text-slate-300 mb-1">{r.motivo}</p>
          <p className="text-sm text-slate-500 mb-4">{r.descripcion}</p>

          {r.resolucionAdministrativa ? (
            <div className={`border rounded-xl px-4 py-3 text-sm ${
              r.resultado === 'ACEPTADO'
                ? 'bg-emerald-500/8 border-emerald-500/20 text-emerald-400'
                : 'bg-slate-800/40 border-slate-700/50 text-slate-400'
            }`}>
              <span className="font-semibold">Resolución:</span> {r.resolucionAdministrativa}
            </div>
          ) : resolviendo === r.id ? (
            <div className="border border-amber-500/20 bg-amber-500/8 rounded-2xl p-4 space-y-3">
              <textarea
                value={comentario}
                onChange={e => setComentario(e.target.value)}
                placeholder="Comentario opcional para el registro de la resolución..."
                rows={2}
                maxLength={500}
                className="w-full px-3.5 py-2.5 bg-slate-900/60 rounded-xl border border-slate-700/60 focus:outline-none focus:ring-2 focus:ring-amber-500/50 focus:border-amber-500/40 text-slate-300 placeholder-slate-600 text-sm resize-none transition-all duration-200"
              />
              <div className="flex items-center gap-2 flex-wrap">
                <button
                  onClick={() => handleResolver(r.id, true)}
                  disabled={accionLoading}
                  className="text-sm bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-700 disabled:text-slate-500 text-white font-semibold px-4 py-2 rounded-xl cursor-pointer transition-all duration-200 shadow-lg shadow-emerald-500/20 disabled:cursor-not-allowed"
                >
                  Aceptar Reclamo (Devolver fondos)
                </button>
                <button
                  onClick={() => handleResolver(r.id, false)}
                  disabled={accionLoading}
                  className="text-sm bg-red-600 hover:bg-red-500 disabled:bg-slate-700 disabled:text-slate-500 text-white font-semibold px-4 py-2 rounded-xl cursor-pointer transition-all duration-200 shadow-lg shadow-red-500/20 disabled:cursor-not-allowed"
                >
                  Rechazar Reclamo (Liberar fondos)
                </button>
                <button
                  onClick={() => { setResolviendo(null); setComentario(''); setError('') }}
                  disabled={accionLoading}
                  className="text-sm text-slate-500 hover:text-slate-300 cursor-pointer transition-colors disabled:cursor-not-allowed"
                >
                  Cancelar
                </button>
              </div>
              {error && <p className="text-red-400 text-xs">{error}</p>}
            </div>
          ) : (
            <button
              onClick={() => { setResolviendo(r.id); setError('') }}
              className="text-sm bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 font-medium px-4 py-2.5 rounded-xl cursor-pointer transition-all duration-200 border border-amber-500/20 hover:border-amber-500/40"
            >
              Resolver disputa
            </button>
          )}
        </div>
      ))}
    </div>
  )
}

export default function AdminPage() {
  const [tab, setTab] = useState('usuarios')

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-10 h-10 bg-amber-500/15 border border-amber-500/20 rounded-xl flex items-center justify-center">
            <svg className="w-5 h-5 text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white tracking-tight">Panel de Administración</h1>
            <p className="text-slate-500 text-sm">Gestión de usuarios y resolución de disputas</p>
          </div>
        </div>
      </div>

      <div className="flex gap-2 mb-7">
        <TabButton active={tab === 'usuarios'} onClick={() => setTab('usuarios')}>Usuarios</TabButton>
        <TabButton active={tab === 'disputas'} onClick={() => setTab('disputas')}>Disputas</TabButton>
      </div>

      {tab === 'usuarios' && <UsuariosPanel />}
      {tab === 'disputas' && <DisputasPanel />}
    </div>
  )
}
