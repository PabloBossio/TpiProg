import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { usuarioService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import Countdown from '../components/Countdown'

const ESTADO_BADGE = {
  ACTIVA:     { label: 'Activa',     cls: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30' },
  PUBLICADA:  { label: 'Publicada',  cls: 'bg-blue-500/20 text-blue-400 border-blue-500/30' },
  FINALIZADA: { label: 'Finalizada', cls: 'bg-slate-500/20 text-slate-400 border-slate-500/30' },
  ADJUDICADA: { label: 'Adjudicada', cls: 'bg-purple-500/20 text-purple-400 border-purple-500/30' },
  CANCELADA:  { label: 'Cancelada',  cls: 'bg-red-500/20 text-red-400 border-red-500/30' },
  BORRADOR:   { label: 'Borrador',   cls: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30' },
  EN_DISPUTA: { label: 'En disputa', cls: 'bg-orange-500/20 text-orange-400 border-orange-500/30' },
}

function TabButton({ active, onClick, children }) {
  return (
    <button
      onClick={onClick}
      className={`px-5 py-2.5 text-sm font-medium rounded-full transition-all duration-200 cursor-pointer ${
        active
          ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
          : 'text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-700/50'
      }`}
    >
      {children}
    </button>
  )
}

export default function ProfilePage() {
  const { user } = useAuth()
  const [tab, setTab] = useState('subastas')
  const [subastas, setSubastas] = useState([])
  const [pujas, setPujas] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user?.id) return
    setLoading(true)
    setError('')
    const req = tab === 'subastas'
      ? usuarioService.subastas(user.id)
      : usuarioService.pujas(user.id)
    req
      .then(({ data }) => { if (tab === 'subastas') setSubastas(data); else setPujas(data) })
      .catch(() => setError('No se pudo cargar la información.'))
      .finally(() => setLoading(false))
  }, [tab, user?.id])

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      {/* Header de perfil */}
      <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 mb-6">
        <div className="flex items-center gap-5">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center text-2xl font-bold text-white shrink-0 shadow-lg shadow-indigo-500/20">
            {user?.username?.[0]?.toUpperCase() ?? '?'}
          </div>
          <div>
            <h1 className="text-xl font-bold text-white">{user?.username}</h1>
            <p className="text-slate-500 text-sm mt-0.5">{user?.email}</p>
          </div>
          <Link
            to="/crear"
            className="ml-auto bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white text-sm font-medium px-4 py-2.5 rounded-xl transition-all duration-300 shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/40"
          >
            + Publicar subasta
          </Link>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6">
        <TabButton active={tab === 'subastas'} onClick={() => setTab('subastas')}>Mis subastas</TabButton>
        <TabButton active={tab === 'pujas'} onClick={() => setTab('pujas')}>Mis pujas</TabButton>
      </div>

      {/* Loading */}
      {loading && (
        <div className="flex justify-center items-center py-20">
          <div className="w-8 h-8 border-2 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
        </div>
      )}

      {error && !loading && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 px-5 py-4 rounded-xl text-sm">
          {error}
        </div>
      )}

      {/* Mis subastas */}
      {!loading && !error && tab === 'subastas' && (
        subastas.length === 0 ? (
          <div className="text-center py-20">
            <div className="w-16 h-16 bg-slate-800/60 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <svg className="w-8 h-8 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </div>
            <p className="text-slate-400 font-medium">Todavía no publicaste ninguna subasta.</p>
            <Link to="/crear" className="text-indigo-400 hover:text-indigo-300 text-sm mt-3 inline-block transition-colors">
              Publicar mi primera subasta →
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {subastas.map((s) => {
              const badge = ESTADO_BADGE[s.estado] ?? { label: s.estado, cls: 'bg-gray-500/20 text-gray-400 border-gray-500/30' }
              return (
                <Link
                  key={s.id}
                  to={`/subastas/${s.id}`}
                  className="flex items-center gap-4 bg-slate-900/60 rounded-2xl border border-slate-800/80 p-4 hover:border-indigo-500/30 hover:shadow-lg hover:shadow-indigo-500/5 hover:-translate-y-0.5 transition-all duration-200 group"
                >
                  {s.producto?.imagenUrl ? (
                    <img
                      src={s.producto.imagenUrl}
                      alt={s.producto.nombre}
                      className="w-14 h-14 rounded-xl object-cover shrink-0 bg-slate-800"
                      onError={e => { e.target.style.display = 'none' }}
                    />
                  ) : (
                    <div className="w-14 h-14 rounded-xl bg-slate-800/80 flex items-center justify-center shrink-0 border border-slate-700/50">
                      <svg className="w-6 h-6 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                  )}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="font-semibold text-slate-200 text-sm truncate group-hover:text-indigo-400 transition-colors">
                        {s.producto?.nombre}
                      </p>
                      <span className={`shrink-0 text-xs font-medium px-2 py-0.5 rounded-full border ${badge.cls}`}>
                        {badge.label}
                      </span>
                    </div>
                    <p className="text-xs text-slate-600">
                      {s.categoria?.nombre} · Cierra {new Date(s.fechaCierre).toLocaleDateString('es-AR')}
                    </p>
                  </div>
                  <div className="text-right shrink-0">
                    <p className="font-bold bg-gradient-to-r from-indigo-400 to-violet-400 bg-clip-text text-transparent text-base">
                      ${Number(s.montoActual).toLocaleString('es-AR')}
                    </p>
                    {s.estado === 'ACTIVA' && (
                      <Countdown fechaCierre={s.fechaCierre} className="justify-end mt-1" />
                    )}
                  </div>
                </Link>
              )
            })}
          </div>
        )
      )}

      {/* Mis pujas */}
      {!loading && !error && tab === 'pujas' && (
        pujas.length === 0 ? (
          <div className="text-center py-20">
            <div className="w-16 h-16 bg-slate-800/60 rounded-2xl flex items-center justify-center mx-auto mb-5">
              <svg className="w-8 h-8 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <p className="text-slate-400 font-medium">Todavía no realizaste ninguna puja.</p>
            <Link to="/" className="text-indigo-400 hover:text-indigo-300 text-sm mt-3 inline-block transition-colors">
              Ver subastas activas →
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {pujas.map(p => (
              <div
                key={p.id}
                className="flex items-center gap-4 bg-slate-900/60 rounded-2xl border border-slate-800/80 p-4"
              >
                <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center shrink-0">
                  <svg className="w-5 h-5 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-slate-300 text-sm">Puja #{p.id}</p>
                  <p className="text-xs text-slate-600">{new Date(p.fechaPuja).toLocaleString('es-AR')}</p>
                </div>
                <p className="font-bold bg-gradient-to-r from-indigo-400 to-violet-400 bg-clip-text text-transparent text-base shrink-0">
                  ${Number(p.monto).toLocaleString('es-AR')}
                </p>
              </div>
            ))}
          </div>
        )
      )}
    </div>
  )
}
