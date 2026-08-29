import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { Dashboard } from './pages/Dashboard'
import { Orders } from './pages/Orders'
import { Tracking } from './pages/Tracking'
import { Placeholder } from './pages/Placeholder'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/tracking" element={<Tracking />} />
          <Route path="/deliveries" element={<Placeholder title="Entregas" />} />
          <Route path="/stock" element={<Placeholder title="Estoque" />} />
          <Route path="/routes" element={<Placeholder title="Rotas" />} />
          <Route path="/settings" element={<Placeholder title="Configurações" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
