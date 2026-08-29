import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { Deliveries } from './pages/Deliveries'
import { Routes as RoutesPage } from './pages/Routes'
import { Drivers } from './pages/Drivers'
import { Drones } from './pages/Drones'
import { Customers } from './pages/Customers'
import { Orders } from './pages/Orders'
import { Tracking } from './pages/Tracking'
import { LiveTracking } from './pages/LiveTracking'
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
          <Route path="/drones" element={<Drones />} />
          <Route path="/customers" element={<Customers />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/tracking" element={<Tracking />} />
          <Route path="/vehicles" element={<Placeholder title="Veículos" />} />
          <Route path="/incidents" element={<Placeholder title="Ocorrências" />} />
          <Route path="/reports" element={<Placeholder title="Relatórios" />} />
          <Route path="/stock" element={<Placeholder title="Estoque" />} />
          <Route path="/settings" element={<Placeholder title="Configurações" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
