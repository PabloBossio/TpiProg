import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <nav className="bg-slate-900/95 backdrop-blur-md border-b border-slate-800/80 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2.5 shrink-0 group">
          <div className="w-8 h-8 bg-gradient-to-br from-indigo-500 to-violet-600 rounded-lg flex items-center justify-center shadow-lg shadow-indigo-500/30 group-hover:shadow-indigo-500/50 transition-shadow duration-300">
            <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 2a8 8 0 100 16A8 8 0 0010 2zm0 14a6 6 0 110-12 6 6 0 010 12zm1-7V5a1 1 0 10-2 0v4a1 1 0 000 1.414l2.5 2.5a1 1 0 001.414-1.414L11 9z" />
            </svg>
          </div>
          <span className="font-bold text-xl bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent tracking-tight">
            SubastasPro
          </span>
        </Link>

        <div className="flex items-center gap-1">
          {user ? (
            <>
              <Link to="/" className="text-sm text-slate-400 hover:text-white font-medium px-3 py-2 rounded-lg hover:bg-slate-800 transition-all duration-200">
                Subastas
              </Link>
              <Link to="/crear" className="hidden sm:flex items-center gap-1 text-sm text-indigo-400 hover:text-indigo-300 font-medium px-3 py-2 rounded-lg hover:bg-indigo-500/10 transition-all duration-200">
                + Publicar
              </Link>
              <Link to="/perfil" className="text-sm text-slate-400 hover:text-white font-medium px-3 py-2 rounded-lg hover:bg-slate-800 transition-all duration-200">
                <span className="hidden sm:inline">Mi perfil</span>
                <span className="sm:hidden text-base">👤</span>
              </Link>
              {isAdmin && (
                <Link to="/admin" className="text-sm bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 font-semibold px-3 py-2 rounded-lg transition-all duration-200 border border-amber-500/20 hover:border-amber-500/40">
                  Admin
                </Link>
              )}
              <button
                onClick={handleLogout}
                className="text-sm bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white font-medium px-3 py-2 rounded-lg transition-all duration-200 cursor-pointer border border-slate-700/50"
              >
                Salir
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm text-slate-400 hover:text-white font-medium px-4 py-2 rounded-lg transition-all duration-200">
                Iniciar sesión
              </Link>
              <Link to="/register" className="text-sm bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white font-medium px-4 py-2 rounded-lg transition-all duration-300 shadow-lg shadow-indigo-500/20">
                Registrarse
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}
