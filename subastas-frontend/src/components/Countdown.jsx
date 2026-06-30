import { useState, useEffect } from 'react'

function calcularTiempo(fechaCierre) {
  const diff = new Date(fechaCierre) - new Date()
  if (diff <= 0) return null
  return {
    dias: Math.floor(diff / 86400000),
    horas: Math.floor((diff % 86400000) / 3600000),
    minutos: Math.floor((diff % 3600000) / 60000),
    segundos: Math.floor((diff % 60000) / 1000),
  }
}

export default function Countdown({ fechaCierre, className = '' }) {
  const [tiempo, setTiempo] = useState(() => calcularTiempo(fechaCierre))

  useEffect(() => {
    const timer = setInterval(() => setTiempo(calcularTiempo(fechaCierre)), 1000)
    return () => clearInterval(timer)
  }, [fechaCierre])

  if (!tiempo) {
    return <span className={`text-red-400 font-semibold text-sm ${className}`}>Finalizada</span>
  }

  const unidades = tiempo.dias > 0
    ? [{ v: tiempo.dias, l: 'd' }, { v: tiempo.horas, l: 'h' }, { v: tiempo.minutos, l: 'm' }]
    : [{ v: tiempo.horas, l: 'h' }, { v: tiempo.minutos, l: 'm' }, { v: tiempo.segundos, l: 's' }]

  const urgente = tiempo.dias === 0 && tiempo.horas < 2

  return (
    <div className={`flex items-center gap-1 ${className}`}>
      {unidades.map(({ v, l }) => (
        <span
          key={l}
          className={`font-mono font-bold text-sm px-1.5 py-0.5 rounded border ${
            urgente
              ? 'bg-red-500/15 text-red-400 border-red-500/30'
              : 'bg-slate-700/80 text-slate-300 border-slate-600/50'
          }`}
        >
          {String(v).padStart(2, '0')}{l}
        </span>
      ))}
    </div>
  )
}
