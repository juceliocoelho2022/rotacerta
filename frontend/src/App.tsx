import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { RequireAuth } from './auth/RequireAuth'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { Deliveries } from './pages/Deliveries'
import { Routes as RoutesPage } from './pages/Routes'
import { Drivers } from './pages/Drivers'
import { DroneOperations } from './pages/DroneOperations'
import { Customers } from './pages/Customers'
import { Orders } from './pages/Orders'
import { Tracking } from './pages/Tracking'
import { LiveTracking } from './pages/LiveTracking'
import { Vehicles } from './pages/Vehicles'
import { Incidents } from './pages/Incidents'
import { Reports } from './pages/Reports'
import { Settings } from './pages/Settings'
import { Stock } from './pages/Stock'
import { Login } from './pages/Login'
import { RolePortal } from './pages/RolePortal'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/live/:token" element={<LiveTracking />} />

          <Route element={<RequireAuth />}>
            <Route path="/portal" element={<RolePortal />} />
          </Route>

          <Route element={<RequireAuth roles={['ADMIN']} />}>
            <Route element={<Layout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/deliveries" element={<Deliveries />} />
              <Route path="/routes" element={<RoutesPage />} />
              <Route path="/drivers" element={<Drivers />} />
              <Route path="/drones" element={<DroneOperations />} />
              <Route path="/customers" element={<Customers />} />
              <Route path="/orders" element={<Orders />} />
              <Route path="/tracking" element={<Tracking />} />
              <Route path="/vehicles" element={<Vehicles />} />
              <Route path="/incidents" element={<Incidents />} />
              <Route path="/reports" element={<Reports />} />
              <Route path="/stock" element={<Stock />} />
              <Route path="/settings" element={<Settings />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
