import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { subastaService, categoriaService } from '../services/api'
import { useAuth } from '../context/AuthContext'

const inputClass = "w-full px-4 py-2.5 bg-slate-800/60 rounded-xl border border-slate-700/60 focus:outline-none focus:ring-2 focus:ring-indigo-500/60 focus:border-indigo-500/50 text-white placeholder-slate-500 text-sm transition-all duration-200"

// Los inputs <input type="datetime-local"> interpretan su value/min/max como hora LOCAL del
// navegador (sin offset). toISOString() devuelve UTC, así que usarlo directo desplaza el
// mínimo varias horas según el huso horario. Este helper formatea en hora local real.
function toLocalInputValue(date) {
  const sinOffset = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return sinOffset.toISOString().slice(0, 16)
}

export default function CreateAuctionPage() {
  const { user, isAdmin } = useAuth()
  const navigate = useNavigate()

  const [categorias, setCategorias] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    nombre: '', descripcion: '', imagenUrl: '', precioBase: '',
    incrementoMinimo: '', categoriaId: '', descripcionSubasta: '',
    fechaInicio: '', fechaCierre: '',
  })

  const [mostrarNuevaCategoria, setMostrarNuevaCategoria] = useState(false)
  const [nuevaCategoria, setNuevaCategoria] = useState('')
  const [categoriaLoading, setCategoriaLoading] = useState(false)
  const [categoriaError, setCategoriaError] = useState('')

  useEffect(() => {
    categoriaService.listar().then(({ data }) => setCategorias(data))
  }, [])

  const handleCrearCategoria = async () => {
    if (!nuevaCategoria.trim()) { setCategoriaError('Escribí un nombre para la categoría.'); return }
    setCategoriaLoading(true); setCategoriaError('')
    try {
      const { data } = await categoriaService.crear(nuevaCategoria.trim())
      setCategorias((prev) => [...prev, data])
      setForm((f) => ({ ...f, categoriaId: String(data.id) }))
      setNuevaCategoria('')
      setMostrarNuevaCategoria(false)
    } catch (err) {
      const msg = err.response?.data?.message ?? 'No se pudo crear la categoría.'
      setCategoriaError(typeof msg === 'string' ? msg : 'No se pudo crear la categoría.')
    } finally {
      setCategoriaLoading(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((f) => {
      const next = { ...f, [name]: value }
      if (name === 'fechaInicio' && next.fechaCierre) {
        const maxCierre = new Date(new Date(value).getTime() + 14 * 86400000)
        if (new Date(next.fechaCierre) > maxCierre) next.fechaCierre = ''
      }
      return next
    })
    setError('')
  }

  const inicioMin = toLocalInputValue(new Date(Date.now() + 60000))
  const baseInicio = form.fechaInicio ? new Date(form.fechaInicio) : new Date()
  const cierreMax = toLocalInputValue(new Date(baseInicio.getTime() + 14 * 86400000))
  const cierreMin = form.fechaInicio
    ? toLocalInputValue(new Date(new Date(form.fechaInicio).getTime() + 60000))
    : inicioMin

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.nombre || !form.precioBase || !form.incrementoMinimo || !form.categoriaId || !form.fechaCierre) {
      setError('Completá todos los campos obligatorios.'); return
    }
    const ahora = new Date()
    // Aproximación solo para la validación en el cliente (UX); si se deja vacía, el
    // servidor asigna su propia hora UTC actual — nunca confiamos en la del navegador.
    const inicioAprox = form.fechaInicio ? new Date(form.fechaInicio) : ahora
    const cierre = new Date(form.fechaCierre)
    if (cierre <= ahora) { setError('La fecha de cierre debe ser en el futuro.'); return }
    if (cierre > new Date(inicioAprox.getTime() + 14 * 86400000)) {
      setError('La fecha de cierre no puede superar los 14 días desde la fecha de inicio.'); return
    }
    setLoading(true)
    try {
      const payload = {
        precioBase: parseFloat(form.precioBase),
        incrementoMinimo: parseFloat(form.incrementoMinimo),
        fechaInicio: form.fechaInicio ? new Date(form.fechaInicio).toISOString().slice(0, 19) : null,
        fechaCierre: cierre.toISOString().slice(0, 19),
        descripcion: form.descripcionSubasta || null,
        categoriaId: parseInt(form.categoriaId),
        producto: {
          nombre: form.nombre,
          descripcion: form.descripcion || null,
          imagenUrl: form.imagenUrl || null,
        },
      }
      const { data } = await subastaService.crear(payload, user.id)
      navigate(`/subastas/${data.id}`)
    } catch (err) {
      const msg = err.response?.data?.message ?? err.response?.data ?? 'Error al crear la subasta.'
      setError(typeof msg === 'string' ? msg : 'Error al crear la subasta.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <button
        onClick={() => navigate('/')}
        className="flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-300 mb-8 cursor-pointer transition-colors duration-200 group"
      >
        <svg className="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
        </svg>
        Volver a subastas
      </button>

      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white tracking-tight">Publicar subasta</h1>
        <p className="text-slate-500 text-sm mt-1">Completá los datos para poner tu producto en subasta</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Producto */}
        <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 space-y-5">
          <h2 className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Producto</h2>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Título <span className="text-red-400">*</span>
            </label>
            <input
              type="text" name="nombre" value={form.nombre} onChange={handleChange}
              placeholder="Ej: Guitarra Gibson Les Paul 2020"
              maxLength={150} className={inputClass}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Descripción del producto</label>
            <textarea
              name="descripcion" value={form.descripcion} onChange={handleChange}
              placeholder="Detallá el estado, características, accesorios incluidos..."
              rows={3} maxLength={1000}
              className={`${inputClass} resize-none`}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">URL de imagen</label>
            <input
              type="url" name="imagenUrl" value={form.imagenUrl} onChange={handleChange}
              placeholder="https://ejemplo.com/imagen.jpg" className={inputClass}
            />
            {form.imagenUrl && (
              <div className="mt-3 rounded-xl overflow-hidden border border-slate-700/60 h-44 bg-slate-800/40">
                <img
                  src={form.imagenUrl} alt="Vista previa"
                  className="w-full h-full object-cover"
                  onError={e => { e.target.style.display = 'none' }}
                />
              </div>
            )}
          </div>
        </div>

        {/* Subasta */}
        <div className="bg-slate-900/60 rounded-2xl border border-slate-800/80 p-6 space-y-5">
          <h2 className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Subasta</h2>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Precio base ($) <span className="text-red-400">*</span>
              </label>
              <input
                type="number" name="precioBase" value={form.precioBase} onChange={handleChange}
                placeholder="1000" min="0.01" step="0.01" className={inputClass}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Incremento mínimo ($) <span className="text-red-400">*</span>
              </label>
              <input
                type="number" name="incrementoMinimo" value={form.incrementoMinimo} onChange={handleChange}
                placeholder="100" min="0.01" step="0.01" className={inputClass}
              />
            </div>
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-slate-300">
                Categoría <span className="text-red-400">*</span>
              </label>
              {isAdmin && !mostrarNuevaCategoria && (
                <button
                  type="button"
                  onClick={() => { setMostrarNuevaCategoria(true); setCategoriaError('') }}
                  className="text-xs text-indigo-400 hover:text-indigo-300 font-medium cursor-pointer transition-colors"
                >
                  + Nueva categoría
                </button>
              )}
            </div>

            {categorias.length === 0 && !mostrarNuevaCategoria && (
              <p className="text-xs text-amber-400/80 mb-2">
                Todavía no hay categorías cargadas.{isAdmin ? ' Creá la primera con el botón de arriba.' : ' Pedile a un administrador que cargue alguna.'}
              </p>
            )}

            {mostrarNuevaCategoria ? (
              <div className="border border-indigo-500/20 bg-indigo-500/8 rounded-xl p-4 space-y-3 mb-3">
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={nuevaCategoria}
                    onChange={(e) => { setNuevaCategoria(e.target.value); setCategoriaError('') }}
                    placeholder="Ej: Instrumentos musicales"
                    maxLength={100}
                    className={inputClass}
                  />
                  <button
                    type="button"
                    onClick={handleCrearCategoria}
                    disabled={categoriaLoading}
                    className="shrink-0 bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-700 text-white text-sm font-semibold px-4 rounded-xl transition-all duration-200 cursor-pointer disabled:cursor-not-allowed"
                  >
                    {categoriaLoading ? '...' : 'Guardar'}
                  </button>
                  <button
                    type="button"
                    onClick={() => { setMostrarNuevaCategoria(false); setNuevaCategoria(''); setCategoriaError('') }}
                    className="shrink-0 text-sm text-slate-500 hover:text-slate-300 cursor-pointer px-2 transition-colors"
                  >
                    Cancelar
                  </button>
                </div>
                {categoriaError && <p className="text-red-400 text-xs">{categoriaError}</p>}
              </div>
            ) : null}

            <select
              name="categoriaId" value={form.categoriaId} onChange={handleChange}
              className={`${inputClass} bg-slate-800/60`}
            >
              <option value="">Seleccioná una categoría</option>
              {categorias.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Fecha y hora de inicio</label>
            <input
              type="datetime-local" name="fechaInicio" value={form.fechaInicio}
              onChange={handleChange} min={inicioMin} className={inputClass}
            />
            <p className="text-xs text-slate-600 mt-1.5">Opcional. Si no elegís, la subasta inicia inmediatamente.</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Fecha y hora de cierre <span className="text-red-400">*</span>
            </label>
            <input
              type="datetime-local" name="fechaCierre" value={form.fechaCierre}
              onChange={handleChange} min={cierreMin} max={cierreMax} className={inputClass}
            />
            <p className="text-xs text-slate-600 mt-1.5">
              Máximo 14 días desde la fecha de inicio
              {form.fechaInicio && (
                <span className="text-indigo-400 font-medium">
                  {' '}(hasta {new Date(cierreMax).toLocaleDateString('es-AR')})
                </span>
              )}
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Descripción adicional</label>
            <textarea
              name="descripcionSubasta" value={form.descripcionSubasta} onChange={handleChange}
              placeholder="Condiciones de envío, forma de pago, etc."
              rows={2} className={`${inputClass} resize-none`}
            />
          </div>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-400 text-sm px-5 py-4 rounded-xl">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 disabled:from-slate-700 disabled:to-slate-700 disabled:text-slate-500 text-white font-semibold py-3.5 rounded-xl transition-all duration-300 shadow-lg shadow-indigo-500/20 hover:shadow-indigo-500/40 cursor-pointer disabled:cursor-not-allowed text-sm"
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              Publicando...
            </span>
          ) : 'Publicar subasta'}
        </button>
      </form>
    </div>
  )
}
