import { NavLink, Outlet } from 'react-router-dom'
import { LayoutDashboard, PackageSearch, Route, Truck, Warehouse, Settings, MapPinned } from 'lucide-react'

const menu = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/orders', label: 'Pedidos', icon: PackageSearch },
  { to: '/tracking', label: 'Rastreamento', icon: MapPinned },
  { to: '/deliveries', label: 'Entregas', icon: Truck },
  { to: '/stock', label: 'Estoque', icon: Warehouse },
  { to: '/routes', label: 'Rotas', icon: Route },
  { to: '/settings', label: 'Configurações', icon: Settings }
]

export function Layout() {
  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brandMark">RC</div>
          <div><strong>RotaCerta</strong><small>Operations</small></div>
        </div>
        <nav>
          {menu.map(({ to, label, icon: Icon }) => (
            <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'nav active' : 'nav'}>
              <Icon size={18} /> {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebarFooter"><span className="healthDot" /> Sistemas operacionais</div>
      </aside>
      <main className="content">
        <header className="topbar">
          <div><h1>RotaCerta</h1><p>Smart E-commerce Logistics & Delivery Platform</p></div>
          <div className="operator">Admin • Operações</div>
        </header>
        <Outlet />
      </main>
    </div>
  )
}
