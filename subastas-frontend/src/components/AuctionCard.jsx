import { Link } from 'react-router-dom'
import Countdown from './Countdown'

const ESTADO_BADGE = {
  ACTIVA:     { label: 'Activa',     cls: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' },
  PUBLICADA:  { label: 'Publicada',  cls: 'bg-blue-500/20 text-blue-400 border border-blue-500/30' },
  FINALIZADA: { label: 'Finalizada', cls: 'bg-slate-500/20 text-slate-400 border border-slate-500/30' },
  ADJUDICADA: { label: 'Adjudicada', cls: 'bg-purple-500/20 text-purple-400 border border-purple-500/30' },
  CANCELADA:  { label: 'Cancelada',  cls: 'bg-red-500/20 text-red-400 border border-red-500/30' },
  BORRADOR:   { label: 'Borrador',   cls: 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30' },
  EN_DISPUTA: { label: 'En disputa', cls: 'bg-orange-500/20 text-orange-400 border border-orange-500/30' },
}

export default function AuctionCard({ subasta }) {
  const badge = ESTADO_BADGE[subasta.estado] ?? { label: subasta.estado, cls: 'bg-gray-500/20 text-gray-400 border border-gray-500/30' }
  const esActiva = subasta.estado === 'ACTIVA'

  return (
    <Link
      to={`/subastas/${subasta.id}`}
      className="group block bg-slate-900/60 rounded-2xl border border-slate-800/80 hover:border-indigo-500/40 hover:shadow-2xl hover:shadow-indigo-500/10 hover:-translate-y-1 transition-all duration-300 overflow-hidden"
    >
      {/* Image */}
      <div className="relative h-44 bg-gradient-to-br from-slate-800 to-slate-900 overflow-hidden">
        {subasta.producto?.imagenUrl ? (
          <img
            src={subasta.producto.imagenUrl}
            alt={subasta.producto.nombre}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            onError={e => { e.target.style.display = 'none' }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <svg className="w-16 h-16 text-slate-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/90 via-slate-900/20 to-transparent" />
        <div className="absolute top-3 right-3">
          <span className={`text-xs font-semibold px-2.5 py-1 rounded-full backdrop-blur-sm ${badge.cls}`}>
            {badge.label}
          </span>
        </div>
        {subasta.categoria?.nombre && (
          <div className="absolute bottom-3 left-3">
            <span className="text-xs text-white/60 bg-black/40 backdrop-blur-sm px-2 py-0.5 rounded-full">
              {subasta.categoria.nombre}
            </span>
          </div>
        )}
      </div>

      <div className="p-5">
        <h3 className="font-semibold text-slate-100 text-base leading-snug group-hover:text-indigo-400 transition-colors line-clamp-2 mb-1">
          {subasta.producto?.nombre ?? 'Producto sin nombre'}
        </h3>
        {subasta.producto?.descripcion && (
          <p className="text-xs text-slate-500 line-clamp-1 mb-2">{subasta.producto.descripcion}</p>
        )}
        <p className="text-xs text-slate-600 mb-4">
          Vendedor: <span className="text-slate-500">{subasta.vendedorNombre}</span>
        </p>

        <div className="border-t border-slate-800/80 pt-4 flex items-end justify-between">
          <div>
            <p className="text-xs text-slate-500 mb-0.5">Oferta actual</p>
            <p className="text-2xl font-bold bg-gradient-to-r from-indigo-400 to-violet-400 bg-clip-text text-transparent">
              ${Number(subasta.montoActual).toLocaleString('es-AR')}
            </p>
            {String(subasta.precioBase) !== String(subasta.montoActual) && (
              <p className="text-xs text-slate-600 mt-0.5">
                Base: ${Number(subasta.precioBase).toLocaleString('es-AR')}
              </p>
            )}
          </div>

          {esActiva && (
            <div className="text-right">
              <p className="text-xs text-slate-500 mb-1">Cierra en</p>
              <Countdown fechaCierre={subasta.fechaCierre} />
            </div>
          )}
        </div>
      </div>
    </Link>
  )
}
