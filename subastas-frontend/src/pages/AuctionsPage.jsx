import { useState, useEffect } from 'react'
import { subastaService } from '../services/api'
import AuctionCard from '../components/AuctionCard'

const ESTADOS = [
  { value: '', label: 'Todas' },
  { value: 'ACTIVA', label: 'Activas' },
  { value: 'PUBLICADA', label: 'Publicadas' },
  { value: 'FINALIZADA', label: 'Finalizadas' },
  { value: 'ADJUDICADA', label: 'Adjudicadas' },
]

function SkeletonCard() {
  return (
    <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 overflow-hidden">
      <div className="h-44 skeleton" />
      <div className="p-5 space-y-3">
        <div className="h-4 skeleton rounded-lg w-3/4" />
        <div className="h-3 skeleton rounded-lg w-1/2" />
        <div className="pt-4 border-t border-slate-800/80 flex justify-between items-end">
          <div className="space-y-1.5">
            <div className="h-2.5 skeleton rounded w-16" />
            <div className="h-6 skeleton rounded w-24" />
          </div>
          <div className="h-6 skeleton rounded w-20" />
        </div>
      </div>
    </div>
  )
}

export default function AuctionsPage() {
  const [subastas, setSubastas] = useState([])
  const [filtro, setFiltro] = useState('ACTIVA')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelado = false
    setLoading(true)
    setError('')
    subastaService.listar(filtro)
      .then(({ data }) => { if (!cancelado) setSubastas(data) })
      .catch(() => { if (!cancelado) setError('No se pudieron cargar las subastas.') })
      .finally(() => { if (!cancelado) setLoading(false) })
    return () => { cancelado = true }
  }, [filtro])

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-1 tracking-tight">Subastas</h1>
        <p className="text-slate-500">Explorá las subastas disponibles y hacé tus pujas</p>
      </div>

      {/* Filtros */}
      <div className="flex flex-wrap gap-2 mb-8">
        {ESTADOS.map(({ value, label }) => (
          <button
            key={value}
            onClick={() => setFiltro(value)}
            className={`px-4 py-2 rounded-full text-sm font-medium transition-all duration-200 cursor-pointer ${
              filtro === value
                ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/20'
                : 'bg-slate-800/70 text-slate-400 border border-slate-700/60 hover:border-indigo-500/40 hover:text-indigo-400 hover:bg-slate-800'
            }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* Loading skeleton */}
      {loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {[1, 2, 3, 4, 5, 6].map(i => <SkeletonCard key={i} />)}
        </div>
      )}

      {/* Error */}
      {error && !loading && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 px-6 py-5 rounded-2xl text-center">
          <p className="font-medium">{error}</p>
          <button
            onClick={() => setFiltro(filtro)}
            className="text-sm text-red-400/70 hover:text-red-400 mt-2 underline cursor-pointer"
          >
            Reintentar
          </button>
        </div>
      )}

      {/* Empty */}
      {!loading && !error && subastas.length === 0 && (
        <div className="text-center py-24">
          <div className="w-20 h-20 bg-slate-800/60 rounded-2xl flex items-center justify-center mx-auto mb-5">
            <svg className="w-10 h-10 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
          </div>
          <p className="text-slate-400 text-lg font-medium">No hay subastas en este estado</p>
          <p className="text-slate-600 text-sm mt-1">Probá con otro filtro o volvé más tarde.</p>
        </div>
      )}

      {/* Grid */}
      {!loading && !error && subastas.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {subastas.map((s) => <AuctionCard key={s.id} subasta={s} />)}
        </div>
      )}
    </div>
  )
}
