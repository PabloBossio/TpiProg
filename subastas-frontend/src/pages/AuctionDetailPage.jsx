import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { subastaService, reclamoService } from '../services/api'
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

const inputClass = "w-full px-3.5 py-2.5 bg-slate-800/60 rounded-xl border border-slate-700/60 focus:outline-none focus:ring-2 focus:ring-indigo-500/60 focus:border-indigo-500/50 text-white placeholder-slate-500 text-sm transition-all duration-200"

export default function AuctionDetailPage() {
  const { id } = useParams()
  const { user, isAdmin } = useAuth()
  const navigate = useNavigate()

  const [subasta, setSubasta] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [monto, setMonto] = useState('')
  const [pujaLoading, setPujaLoading] = useState(false)
  const [pujaError, setPujaError] = useState('')
  const [pujaOk, setPujaOk] = useState('')

  const [showCancelar, setShowCancelar] = useState(false)
  const [motivoCancelar, setMotivoCancelar] = useState('')
  const [cancelLoading, setCancelLoading] = useState(false)
  const [cancelError, setCancelError] = useState('')

  const [showDisputa, setShowDisputa] = useState(false)
  const [disputaForm, setDisputaForm] = useState({ motivo: '', descripcion: '' })
  const [disputaLoading, setDisputaLoading] = useState(false)
  const [disputaError, setDisputaError] = useState('')
  const [disputaOk, setDisputaOk] = useState('')

  const cargarSubasta = useCallback(() => {
    subastaService.obtener(id)
      .then(({ data }) => setSubasta(data))
      .catch(() => setError('No se pudo cargar la subasta.'))
      .finally(() => setLoading(false))
  }, [id])

  useEffect(() => {
    cargarSubasta()
    const interval = setInterval(() => {
      subastaService.obtener(id).then(({ data }) => setSubasta(data)).catch(() => {})
    }, 15000)
    return () => clearInterval(interval)
  }, [id, cargarSubasta])

  const minimoOferta = subasta
    ? (subasta.pujas?.length === 0
        ? Number(subasta.precioBase)
        : Number(subasta.montoActual) + Number(subasta.incrementoMinimo))
    : 0

  const handlePujar = async (e) => {
    e.preventDefault()
    setPujaError(''); setPujaOk('')
    const montoNum = parseFloat(monto)
    if (!monto || isNaN(montoNum) || montoNum <= 0) { setPujaError('Ingresá un monto válido.'); return }
    if (montoNum < minimoOferta) { setPujaError(`Mínimo: $${minimoOferta.toLocaleString('es-AR')}.`); return }
    if (!user?.id) { setPujaError('No se puede identificar tu usuario. Reingresá.'); return }
    setPujaLoading(true)
    try {
      const { data } = await subastaService.pujar(id, user.id, montoNum)
      setSubasta(data); setMonto('')
      setPujaOk(`¡Puja de $${montoNum.toLocaleString('es-AR')} realizada!`)
    } catch (err) {
      if (err.response?.status === 409) {
        const updated = await subastaService.obtener(id).then(r => r.data).catch(() => null)
        if (updated) setSubasta(updated)
        const nuevoMonto = updated
          ? `$${Number(updated.montoActual + Number(updated.incrementoMinimo)).toLocaleString('es-AR')}`
          : 'el monto actualizado'
        setPujaError(`Otro usuario pujó al mismo tiempo. El monto fue actualizado. Ingresá una oferta mayor a ${nuevoMonto}.`)
        setMonto(''); return
      }
      const msg = err.response?.data?.message ?? err.response?.data ?? 'No se pudo realizar la puja.'
      setPujaError(typeof msg === 'string' ? msg : 'No se pudo realizar la puja.')
    } finally { setPujaLoading(false) }
  }

  const handleCancelar = async () => {
    if (!motivoCancelar.trim()) { setCancelError('El motivo es obligatorio.'); return }
    setCancelLoading(true); setCancelError('')
    try {
      const { data } = await subastaService.cancelar(id, motivoCancelar)
      setSubasta(data); setShowCancelar(false); setMotivoCancelar('')
    } catch (err) {
      const msg = err.response?.data?.message ?? err.response?.data ?? 'No se pudo cancelar.'
      setCancelError(typeof msg === 'string' ? msg : 'No se pudo cancelar.')
    } finally { setCancelLoading(false) }
  }

  const handleDisputa = async () => {
    if (!disputaForm.motivo.trim() || !disputaForm.descripcion.trim()) {
      setDisputaError('Completá el motivo y la descripción.'); return
    }
    if (!user?.id) { setDisputaError('Sesión inválida.'); return }
    setDisputaLoading(true); setDisputaError('')
    try {
      await reclamoService.abrir(subasta.id, user.id, disputaForm.motivo, disputaForm.descripcion)
      setDisputaOk('Disputa abierta correctamente.')
      setShowDisputa(false)
      cargarSubasta()
    } catch (err) {
      const msg = err.response?.data?.message ?? err.response?.data ?? 'No se pudo abrir la disputa.'
      setDisputaError(typeof msg === 'string' ? msg : 'Error al abrir la disputa.')
    } finally { setDisputaLoading(false) }
  }

  if (loading) return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="h-5 skeleton rounded-lg w-32 mb-8" />
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-5">
          <div className="h-64 skeleton rounded-2xl" />
          <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 space-y-3">
            <div className="h-6 skeleton rounded w-2/3" />
            <div className="h-4 skeleton rounded w-1/3" />
            <div className="h-3 skeleton rounded w-full mt-4" />
            <div className="h-3 skeleton rounded w-4/5" />
          </div>
        </div>
        <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 h-72 skeleton" />
      </div>
    </div>
  )

  if (error) return (
    <div className="max-w-2xl mx-auto px-4 py-16 text-center">
      <div className="w-16 h-16 bg-red-500/10 rounded-2xl flex items-center justify-center mx-auto mb-5">
        <svg className="w-8 h-8 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <p className="text-slate-400 mb-5">{error}</p>
      <button onClick={() => navigate('/')} className="text-indigo-400 hover:text-indigo-300 font-medium cursor-pointer transition-colors">
        ← Volver a subastas
      </button>
    </div>
  )

  const badge = ESTADO_BADGE[subasta.estado] ?? { label: subasta.estado, cls: 'bg-gray-500/20 text-gray-400 border-gray-500/30' }
  const esActiva = subasta.estado === 'ACTIVA'
  const esAdjudicada = subasta.estado === 'ADJUDICADA'
  const esVendedor = user?.username === subasta.vendedorNombre
  const esGanador = user?.username === subasta.ganadorNombre
  const disputaVencida = (() => {
    if (!subasta.fechaAdjudicacion) return false
    return Date.now() > new Date(subasta.fechaAdjudicacion).getTime() + 86400000
  })()
  const puedeAbriDisputa = esAdjudicada && (esVendedor || esGanador)
    && subasta.estado !== 'EN_DISPUTA' && !disputaVencida
  const puedeCancelar = isAdmin || (esVendedor && (esActiva || subasta.estado === 'PUBLICADA'))
  const pujas = subasta.pujas ?? []

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <button
        onClick={() => navigate('/')}
        className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-300 mb-7 cursor-pointer transition-colors duration-200 group"
      >
        <svg className="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Volver a subastas
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Columna izquierda */}
        <div className="lg:col-span-2 space-y-5">
          {/* Imagen */}
          {subasta.producto?.imagenUrl && (
            <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 overflow-hidden">
              <img
                src={subasta.producto.imagenUrl}
                alt={subasta.producto.nombre}
                className="w-full h-72 object-cover"
                onError={e => { e.target.parentElement.style.display = 'none' }}
              />
            </div>
          )}

          {/* Info producto */}
          <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6">
            <div className="flex items-start justify-between gap-3 mb-4">
              <h1 className="text-2xl font-bold text-white leading-tight">{subasta.producto?.nombre}</h1>
              <span className={`shrink-0 text-xs font-semibold px-3 py-1.5 rounded-full border ${badge.cls}`}>
                {badge.label}
              </span>
            </div>
            <div className="flex flex-wrap gap-2 mb-5">
              {subasta.categoria?.nombre && (
                <span className="bg-slate-800/80 text-slate-400 text-xs px-3 py-1 rounded-full border border-slate-700/50">
                  {subasta.categoria.nombre}
                </span>
              )}
              <span className="bg-slate-800/80 text-slate-400 text-xs px-3 py-1 rounded-full border border-slate-700/50">
                Vendedor: {subasta.vendedorNombre}
              </span>
            </div>
            {subasta.producto?.descripcion && (
              <div className="mb-4">
                <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Sobre el producto</h3>
                <p className="text-slate-300 text-sm leading-relaxed">{subasta.producto.descripcion}</p>
              </div>
            )}
            {subasta.descripcion && (
              <div>
                <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">Condiciones</h3>
                <p className="text-slate-300 text-sm leading-relaxed">{subasta.descripcion}</p>
              </div>
            )}
          </div>

          {/* Detalles */}
          <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6">
            <h2 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">Detalles</h2>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-4 text-sm">
              <div>
                <dt className="text-slate-500 text-xs mb-1">Precio base</dt>
                <dd className="font-semibold text-slate-200">${Number(subasta.precioBase).toLocaleString('es-AR')}</dd>
              </div>
              <div>
                <dt className="text-slate-500 text-xs mb-1">Incremento mínimo</dt>
                <dd className="font-semibold text-slate-200">${Number(subasta.incrementoMinimo).toLocaleString('es-AR')}</dd>
              </div>
              <div>
                <dt className="text-slate-500 text-xs mb-1">Inicio</dt>
                <dd className="font-semibold text-slate-200">{new Date(subasta.fechaInicio).toLocaleString('es-AR')}</dd>
              </div>
              <div>
                <dt className="text-slate-500 text-xs mb-1">Cierre</dt>
                <dd className="font-semibold text-slate-200">{new Date(subasta.fechaCierre).toLocaleString('es-AR')}</dd>
              </div>
              {subasta.ganadorNombre && subasta.ganadorNombre !== 'Sin pujas' && (
                <div className="col-span-2">
                  <dt className="text-slate-500 text-xs mb-1">Mejor postor</dt>
                  <dd className="font-semibold text-indigo-400">{subasta.ganadorNombre}</dd>
                </div>
              )}
            </dl>
          </div>

          {/* Historial de pujas */}
          <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Historial de pujas</h2>
              <span className="text-xs bg-slate-800/80 text-slate-400 px-2.5 py-1 rounded-full border border-slate-700/50">
                {pujas.length} {pujas.length === 1 ? 'puja' : 'pujas'}
              </span>
            </div>
            {esActiva && !isAdmin && (
              <div className="flex items-start gap-2.5 bg-amber-500/8 border border-amber-500/20 px-4 py-3 rounded-xl mb-4">
                <svg className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p className="text-xs text-amber-400/80">Las identidades de los oferentes están ocultas mientras la subasta esté activa.</p>
              </div>
            )}
            {pujas.length === 0 ? (
              <div className="text-center py-10">
                <div className="w-12 h-12 bg-slate-800/60 rounded-xl flex items-center justify-center mx-auto mb-3">
                  <svg className="w-6 h-6 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <p className="text-slate-500 text-sm">Todavía no hay pujas. ¡Sé el primero!</p>
              </div>
            ) : (
              <div className="space-y-2">
                {pujas.map((p, idx) => (
                  <div
                    key={p.id}
                    className={`flex items-center gap-3 p-3.5 rounded-xl border transition-colors ${
                      idx === 0
                        ? 'bg-indigo-500/10 border-indigo-500/20'
                        : 'bg-slate-800/40 border-slate-700/30'
                    }`}
                  >
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${
                      idx === 0 ? 'bg-indigo-500 text-white shadow-lg shadow-indigo-500/30' : 'bg-slate-700 text-slate-400'
                    }`}>
                      {idx + 1}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className={`font-semibold text-sm ${idx === 0 ? 'text-indigo-300' : 'text-slate-300'}`}>
                        {p.oferenteNombre}
                        {idx === 0 && (
                          <span className="ml-2 text-xs font-normal bg-indigo-500/15 text-indigo-400 px-1.5 py-0.5 rounded-full border border-indigo-500/20">
                            Mejor oferta
                          </span>
                        )}
                      </p>
                      <p className="text-xs text-slate-500">{new Date(p.fechaPuja).toLocaleString('es-AR')}</p>
                    </div>
                    <p className={`font-bold text-base shrink-0 ${idx === 0 ? 'text-indigo-400' : 'text-slate-400'}`}>
                      ${Number(p.monto).toLocaleString('es-AR')}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Acciones */}
          {(puedeCancelar || puedeAbriDisputa) && (
            <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 space-y-4">
              <h2 className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Acciones</h2>

              {puedeCancelar && subasta.estado !== 'CANCELADA' && subasta.estado !== 'FINALIZADA' && (
                !showCancelar ? (
                  <button
                    onClick={() => setShowCancelar(true)}
                    className="text-sm bg-red-500/10 hover:bg-red-500/20 text-red-400 font-medium px-4 py-2.5 rounded-xl cursor-pointer transition-all duration-200 border border-red-500/20 hover:border-red-500/40"
                  >
                    Cancelar subasta
                  </button>
                ) : (
                  <div className="border border-red-500/20 bg-red-500/8 rounded-2xl p-5 space-y-3">
                    <p className="text-sm font-medium text-red-400">¿Seguro que querés cancelar esta subasta?</p>
                    <textarea
                      value={motivoCancelar}
                      onChange={e => { setMotivoCancelar(e.target.value); setCancelError('') }}
                      placeholder="Motivo de cancelación (obligatorio)"
                      rows={2}
                      className="w-full px-3.5 py-2.5 bg-slate-900/60 rounded-xl border border-red-500/20 focus:outline-none focus:ring-2 focus:ring-red-500/40 text-slate-300 placeholder-slate-600 text-sm resize-none transition-all duration-200"
                    />
                    {cancelError && <p className="text-red-400 text-xs">{cancelError}</p>}
                    <div className="flex gap-2">
                      <button
                        onClick={handleCancelar}
                        disabled={cancelLoading}
                        className="text-sm bg-red-600 hover:bg-red-500 disabled:bg-red-900 disabled:text-red-700 text-white font-semibold px-4 py-2 rounded-xl cursor-pointer transition-all duration-200"
                      >
                        {cancelLoading ? 'Cancelando...' : 'Confirmar cancelación'}
                      </button>
                      <button
                        onClick={() => { setShowCancelar(false); setCancelError('') }}
                        className="text-sm text-slate-500 hover:text-slate-300 cursor-pointer px-3 transition-colors"
                      >
                        Volver
                      </button>
                    </div>
                  </div>
                )
              )}

              {esAdjudicada && (esVendedor || esGanador) && disputaVencida && (
                <div className="bg-slate-800/40 border border-slate-700/50 text-slate-500 text-sm px-4 py-3 rounded-xl">
                  El plazo de 24 horas para abrir una disputa sobre esta subasta ha expirado.
                </div>
              )}

              {puedeAbriDisputa && (
                disputaOk ? (
                  <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm px-4 py-3 rounded-xl">{disputaOk}</div>
                ) : !showDisputa ? (
                  <button
                    onClick={() => setShowDisputa(true)}
                    className="text-sm bg-orange-500/10 hover:bg-orange-500/20 text-orange-400 font-medium px-4 py-2.5 rounded-xl cursor-pointer transition-all duration-200 border border-orange-500/20 hover:border-orange-500/40"
                  >
                    Abrir disputa
                  </button>
                ) : (
                  <div className="border border-orange-500/20 bg-orange-500/8 rounded-2xl p-5 space-y-3">
                    <p className="text-sm font-medium text-orange-400">Abrir disputa sobre esta subasta</p>
                    <input
                      type="text"
                      value={disputaForm.motivo}
                      onChange={e => setDisputaForm(f => ({ ...f, motivo: e.target.value }))}
                      placeholder="Motivo (ej: producto no recibido)"
                      className="w-full px-3.5 py-2.5 bg-slate-900/60 rounded-xl border border-orange-500/20 focus:outline-none focus:ring-2 focus:ring-orange-500/40 text-slate-300 placeholder-slate-600 text-sm transition-all duration-200"
                    />
                    <textarea
                      value={disputaForm.descripcion}
                      onChange={e => setDisputaForm(f => ({ ...f, descripcion: e.target.value }))}
                      placeholder="Descripción detallada del problema..."
                      rows={3}
                      className="w-full px-3.5 py-2.5 bg-slate-900/60 rounded-xl border border-orange-500/20 focus:outline-none focus:ring-2 focus:ring-orange-500/40 text-slate-300 placeholder-slate-600 text-sm resize-none transition-all duration-200"
                    />
                    {disputaError && <p className="text-red-400 text-xs">{disputaError}</p>}
                    <div className="flex gap-2">
                      <button
                        onClick={handleDisputa}
                        disabled={disputaLoading}
                        className="text-sm bg-orange-500 hover:bg-orange-400 disabled:bg-orange-900 disabled:text-orange-700 text-white font-semibold px-4 py-2 rounded-xl cursor-pointer transition-all duration-200"
                      >
                        {disputaLoading ? 'Enviando...' : 'Enviar disputa'}
                      </button>
                      <button
                        onClick={() => { setShowDisputa(false); setDisputaError('') }}
                        className="text-sm text-slate-500 hover:text-slate-300 cursor-pointer px-3 transition-colors"
                      >
                        Cancelar
                      </button>
                    </div>
                  </div>
                )
              )}
            </div>
          )}
        </div>

        {/* Panel de puja (columna derecha) */}
        <div className="lg:col-span-1">
          <div className="bg-slate-900/80 backdrop-blur-sm rounded-2xl border border-slate-800/80 p-6 sticky top-24">
            {/* Precio actual */}
            <div className="text-center mb-6 pb-6 border-b border-slate-800/80">
              <p className="text-xs text-slate-500 uppercase tracking-wider mb-2">Oferta actual</p>
              <p className="text-4xl font-bold bg-gradient-to-r from-indigo-400 to-violet-400 bg-clip-text text-transparent">
                ${Number(subasta.montoActual).toLocaleString('es-AR')}
              </p>
            </div>

            {/* Countdown */}
            {esActiva && (
              <div className="flex flex-col items-center mb-6 pb-6 border-b border-slate-800/80">
                <p className="text-xs text-slate-500 uppercase tracking-wider mb-3">Tiempo restante</p>
                <Countdown fechaCierre={subasta.fechaCierre} />
              </div>
            )}

            {/* Formulario de puja */}
            {esActiva && !esVendedor ? (
              <form onSubmit={handlePujar} className="space-y-3">
                <div>
                  <label className="block text-sm font-medium text-slate-300 mb-2">Tu oferta</label>
                  <div className="relative">
                    <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-medium text-sm">$</span>
                    <input
                      type="number"
                      value={monto}
                      onChange={e => { setMonto(e.target.value); setPujaError(''); setPujaOk('') }}
                      placeholder={minimoOferta.toLocaleString('es-AR')}
                      min={minimoOferta}
                      step="0.01"
                      className="w-full pl-8 pr-3.5 py-2.5 bg-slate-800/60 rounded-xl border border-slate-700/60 focus:outline-none focus:ring-2 focus:ring-indigo-500/60 focus:border-indigo-500/50 text-white text-sm transition-all duration-200"
                    />
                  </div>
                  <p className="text-xs text-slate-600 mt-1.5">
                    Mínimo: <span className="text-slate-500">${minimoOferta.toLocaleString('es-AR')}</span>
                  </p>
                </div>
                {pujaError && (
                  <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs px-3.5 py-2.5 rounded-xl">
                    {pujaError}
                  </div>
                )}
                {pujaOk && (
                  <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs px-3.5 py-2.5 rounded-xl">
                    {pujaOk}
                  </div>
                )}
                <button
                  type="submit"
                  disabled={pujaLoading}
                  className="w-full bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 disabled:from-slate-700 disabled:to-slate-700 disabled:text-slate-500 text-white font-semibold py-3 rounded-xl transition-all duration-300 shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/40 cursor-pointer disabled:cursor-not-allowed text-sm"
                >
                  {pujaLoading ? (
                    <span className="flex items-center justify-center gap-2">
                      <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                      Procesando...
                    </span>
                  ) : 'Pujar ahora'}
                </button>
              </form>
            ) : esVendedor ? (
              <div className="bg-amber-500/10 border border-amber-500/20 text-amber-400 text-sm px-4 py-3.5 rounded-xl text-center">
                Sos el vendedor de esta subasta.
              </div>
            ) : (
              <div className="bg-slate-800/40 border border-slate-700/40 text-slate-500 text-sm px-4 py-3.5 rounded-xl text-center">
                Esta subasta no acepta pujas en este momento.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
