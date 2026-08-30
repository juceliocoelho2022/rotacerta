import { useEffect, useState } from 'react'
import { LogOut, PackageCheck, Route, ShieldCheck, Truck, UserRound } from 'lucide-react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { api, type Order } from '../services/api'

type DriverProfile = {
  driverId: number | null
  accountName: string
  email: string
  driverName: string | null
  available: boolean
  currentLoad: number
  maxCapacity: number
  vehiclePlate: string | null
  vehicleModel: string | null
  photoUrl: string | null
}

function money(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

export function RolePortal() {
  const { user, logout } = useAuth()
  const [orders, setOrders] = useState<Order[]>([])
  const [driver, setDriver] = useState<DriverProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      if (!user || user.role === 'ADMIN') {
        setLoading(false)
        return
      }
      try {
        if (user.role === 'CUSTOMER') {
          const { data } = await api.get<Order[]>('/api/customer-portal/orders')
          setOrders(data)
        } else {
          const { data } = await api.get<DriverProfile>('/api/driver-portal/me')
          setDriver(data)
        }
      } catch (requestError: any) {
        setError(requestError?.response?.data?.message ?? 'Não foi possível carregar o portal do seu perfil.')
      } finally {
        setLoading(false)
      }
    }
    void load()
  }, [user])

  if (!user) return null
  if (user.role === 'ADMIN') return <Navigate to="/" replace />

  return (
    <main className="rolePortalPage">
      <header className="rolePortalHeader">
        <div>
          <span className="loginEyebrow">Acesso protegido por RBAC</span>
          <h1>{user.role === 'CUSTOMER' ? 'Portal do Cliente' : 'Portal do Motorista'}</h1>
          <p>{user.displayName} · {user.email}</p>
        </div>
        <button onClick={() => void logout()}><LogOut size={17}/> Sair</button>
      </header>

      <section className="roleSecurityBanner">
        <ShieldCheck size={22}/>
        <div><strong>Sessão autenticada</strong><span>Perfil ativo: {user.role}. O backend valida o token e as permissões em cada requisição.</span></div>
      </section>

      {loading && <div className="rolePortalState">Carregando dados autorizados...</div>}
      {error && <div className="rolePortalState error">{error}</div>}

      {!loading && !error && user.role === 'CUSTOMER' && (
        <section className="customerPortalSection">
          <div className="roleSectionTitle"><UserRound size={20}/><div><h2>Meus pedidos</h2><p>Somente pedidos vinculados ao cliente desta conta.</p></div></div>
          <div className="portalOrders">
            {orders.length === 0 ? <div className="rolePortalState">Nenhum pedido vinculado a esta conta.</div> : orders.map(order => (
              <article key={order.id}>
                <div><strong>{order.orderNumber}</strong><span>{order.trackingCode}</span></div>
                <span className="portalStatus">{order.status}</span>
                <div><small>{order.deliveryType}</small><strong>{money(Number(order.total))}</strong></div>
              </article>
            ))}
          </div>
        </section>
      )}

      {!loading && !error && user.role === 'DRIVER' && (
        <section className="driverPortalSection">
          <div className="roleSectionTitle"><Truck size={20}/><div><h2>Minha operação</h2><p>Identidade operacional vinculada à conta autenticada.</p></div></div>
          <div className="driverPortalGrid">
            <article><Truck size={20}/><span>Motorista</span><strong>{driver?.driverName ?? 'Sem vínculo'}</strong></article>
            <article><PackageCheck size={20}/><span>Carga atual</span><strong>{driver ? `${driver.currentLoad}/${driver.maxCapacity}` : '—'}</strong></article>
            <article><Route size={20}/><span>Veículo</span><strong>{driver?.vehiclePlate ?? '—'}</strong><small>{driver?.vehicleModel ?? ''}</small></article>
            <article><ShieldCheck size={20}/><span>Disponibilidade</span><strong>{driver?.available ? 'Disponível' : 'Indisponível'}</strong></article>
          </div>
        </section>
      )}
    </main>
  )
}
