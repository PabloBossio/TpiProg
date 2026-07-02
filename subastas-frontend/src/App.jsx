import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'
import ErrorBoundary from './components/ErrorBoundary'
import ToastContainer from './components/ToastContainer'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AuctionsPage from './pages/AuctionsPage'
import AuctionDetailPage from './pages/AuctionDetailPage'
import CreateAuctionPage from './pages/CreateAuctionPage'
import ProfilePage from './pages/ProfilePage'
import AdminPage from './pages/AdminPage'
import AdminRoute from './components/AdminRoute'

function Layout({ children }) {
  return (
    <div className="min-h-screen bg-slate-950">
      <Navbar />
      <main>{children}</main>
    </div>
  )
}

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <ToastContainer />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route element={<ProtectedRoute />}>
              <Route path="/" element={<Layout><AuctionsPage /></Layout>} />
              <Route path="/subastas/:id" element={<Layout><AuctionDetailPage /></Layout>} />
              <Route path="/crear" element={<Layout><CreateAuctionPage /></Layout>} />
              <Route path="/perfil" element={<Layout><ProfilePage /></Layout>} />
            </Route>

            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<Layout><AdminPage /></Layout>} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  )
}
