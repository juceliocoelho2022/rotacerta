import { BrowserRouter, Route, Routes } from 'react-router-dom'
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
import { Placeholder } from './pages/Placeholder'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/live/:token" element={<LiveTracking />} />
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
          <Route path="/stock" element={<Placeholder title="Estoque" />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
