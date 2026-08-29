import { useEffect, useState } from 'react'
import { api, type DashboardData, type Order } from '../services/api'
import { StatusBadge } from '../components/StatusBadge'
import { Package, Truck, CircleCheckBig, TriangleAlert, Boxes, Route } from 'lucide-react'

export function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => {
    Promise.all([
      api.get<DashboardData>('/api/dashboard'),
      api.get<Order[]>('/api/orders')
    ]).then(([dashboard, orderList]) => {
      setData(dashboard.data)
      setOrders(orderList.data)
    })
  }, [])

  const cards = [
    ['Pedidos', data?.totalOrders ?? 0, Package],
    ['Separação', data?.picking ?? 0, Boxes],
    ['Em transporte', data?.inTransit ?? 0, Truck],
    ['Saiu para entrega', data?.outForDelivery ?? 0, Route],
    ['Entregues', data?.delivered ?? 0, CircleCheckBig],
    ['Falhas', data?.failed ?? 0, TriangleAlert]
  ] as const

  return (
    <>
      <section className="cards">
        {cards.map(([label, value, Icon]) => (
          <article className="metric" key={label}>
            <div className="metricIcon"><Icon size={22} /></div>
            <div><span>{label}</span><strong>{value}</strong></div>
          </article>
        ))}
      </section>

      <section className="grid2">
        <article className="panel">
          <div className="panelHeader"><div><h2>Operação em tempo real</h2><p>Visão resumida do fluxo de entregas</p></div><span className="pill">Live</span></div>
          <div className="process">
            {['Pedido', 'Pagamento', 'Separação', 'Embalagem', 'Transporte', 'Entrega'].map((step, i) => (
              <div className="processStep" key={step}><span>{i + 1}</span><b>{step}</b></div>
            ))}
          </div>
        </article>

        <article className="panel">
          <div className="panelHeader"><div><h2>Saúde da plataforma</h2><p>Serviços principais</p></div></div>
          <div className="healthList">
            {['API Spring Boot', 'PostgreSQL', 'Frontend React', 'Rastreamento'].map(item => (
              <div key={item}><span className="healthDot" /> {item}<b>Healthy</b></div>
            ))}
          </div>
        </article>
      </section>

      <article className="panel">
        <div className="panelHeader"><div><h2>Últimos pedidos</h2><p>Acompanhamento operacional</p></div></div>
        <div className="tableWrap">
          <table>
            <thead><tr><th>Pedido</th><th>Cliente</th><th>Rastreio</th><th>Status</th><th>Total</th></tr></thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>#{order.orderNumber}</td><td>{order.customerName}</td><td className="mono">{order.trackingCode}</td>
                  <td><StatusBadge status={order.status} /></td>
                  <td>R$ {order.total.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </article>
    </>
  )
}
