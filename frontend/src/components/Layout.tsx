import { NavLink, Outlet } from 'react-router-dom'
import {
  BarChart3,
  Bell,
  CarFront,
  ChevronDown,
  CircleAlert,
  LayoutDashboard,
  PackageCheck,
  PackageSearch,
  Route,
  Search,
  Settings,
  UserRound,
  UsersRound
} from 'lucide-react'

const menu = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/deliveries', label: 'Entregas', icon: PackageCheck },
  { to: '/routes', label: 'Rotas', icon: Route },
  { to: '/drivers', label: 'Motoristas', icon: UsersRound },
  { to: '/vehicles', label: 'Veículos', icon: CarFront },
  { to: '/customers', label: 'Clientes', icon: UserRound },
  { to: '/orders', label: 'Pedidos', icon: PackageSearch },
  { to: '/incidents', label: 'Ocorrências', icon: CircleAlert },
  { to: '/reports', label: 'Relatórios', icon: BarChart3 },
  { to: '/settings', label: 'Configurações', icon: Settings }
]

export function Layout() {
  return (
    <div className="shell adminShell">
      <aside className="sidebar adminSidebar">
        <div className="brand adminBrand">
          <div className="brandMark">RC</div>
          <div><strong>RotaCerta</strong><small>Painel de Administrador</small></div>
        </div>

        <nav className="adminNav">
          {menu.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => isActive ? 'nav active' : 'nav'}
            >
              <Icon size={18} />
              <span>{label}</span>
              {['Entregas', 'Rotas', 'Relatórios', 'Configurações'].includes(label) && <ChevronDown size={14} className="navChevron" />}
            </NavLink>
          ))}
        </nav>

        <div className="sidebarFooter adminSidebarFooter">
          <span className="healthDot" /> Operação conectada
          <small>Smart Dispatch + RotaCerta Live</small>
        </div>
      </aside>

      <main className="content adminContent">
        <header className="topbar adminTopbar">
          <div className="topbarContext">
            <span>Central de Operações</span>
            <small>Last-mile intelligence</small>
          </div>
          <div className="topbarActions">
            <button aria-label="Pesquisar"><Search size={19} /></button>
            <button aria-label="Notificações" className="notificationButton"><Bell size={19} /><i>3</i></button>
            <button aria-label="Configurações"><Settings size={19} /></button>
            <div className="adminProfile">
              <div className="adminAvatar">A</div>
              <div><strong>Administrador</strong><small>admin@rotacerta.local</small></div>
              <ChevronDown size={15} />
            </div>
          </div>
        </header>
        <Outlet />
      </main>
    </div>
  )
}
