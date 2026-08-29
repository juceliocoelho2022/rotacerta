import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
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
          <Route path="/orders" element={<Orders />} />
          <Route path="/tracking" element={<Tracking />} />
          <Route path="/deliveries" element={<Placeholder title="Entregas" />} />
          <Route path="/routes" element={<Placeholder title="Rotas" />} />
          <Route path="/drivers" element={<Placeholder title="Motoristas" />} />
          <Route path="/vehicles" element={<Placeholder title="Veículos" />} />
          <Route path="/customers" element={<Placeholder title="Clientes" />} />
          <Route path="/incidents" element={<Placeholder title="Ocorrências" />} />
          <Route path="/reports" element={<Placeholder title="Relatórios" />} />
          <Route path="/stock" element={<Placeholder title="Estoque" />} />
          <Route path="/settings" element={<Placeholder title="Configurações" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
