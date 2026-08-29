import { useEffect, useState } from 'react'
import { ExternalLink } from 'lucide-react'
import { api, type DeliveryStatus, type Order } from '../services/api'
import { StatusBadge } from '../components/StatusBadge'

type LiveLinkResponse = {
  publicUrl: string
  expiresAt: string
}

export function Orders() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [liveLoadingId, setLiveLoadingId] = useState<number | null>(null)

  const load = () => {
    setLoading(true)
    api.get<Order[]>('/api/orders')
      .then(r => setOrders(r.data))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function changeStatus(id: number, status: DeliveryStatus) {
    await api.patch(`/api/deliveries/${id}/status`, {
      status,
      location: 'Atualização pela central'
    })
    load()
  }

  async function openLiveTracking(id: number) {
    setLiveLoadingId(id)
    try {
      const response = await api.post<LiveLinkResponse>(`/api/deliveries/${id}/live-link`)
      window.open(response.data.publicUrl, '_blank', 'noopener,noreferrer')
    } finally {
      setLiveLoadingId(null)
    }
  }

  return (
    <article className="panel">
      <div className="panelHeader">
        <div>
          <h2>Pedidos e entregas</h2>
          <p>Gestão operacional de ponta a ponta</p>
        </div>
      </div>

      {loading ? <p>Carregando...</p> : (
        <div className="tableWrap">
          <table>
            <thead>
              <tr>
                <th>Pedido</th>
                <th>Cliente</th>
                <th>Código</th>
                <th>Status</th>
                <th>Atualizar</th>
                <th>RotaCerta Live</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>#{order.orderNumber}</td>
                  <td>{order.customerName}</td>
                  <td className="mono">{order.trackingCode}</td>
                  <td><StatusBadge status={order.status} /></td>
                  <td>
                    <select
                      value={order.status}
                      onChange={e => changeStatus(order.id, e.target.value as DeliveryStatus)}
                    >
                      <option value="PICKING">Em separação</option>
                      <option value="PACKING">Embalagem</option>
                      <option value="IN_TRANSIT">Em transporte</option>
                      <option value="OUT_FOR_DELIVERY">Saiu para entrega</option>
                      <option value="DELIVERED">Entregue</option>
                      <option value="DELIVERY_FAILED">Falha na entrega</option>
                    </select>
                  </td>
                  <td>
                    <button
                      className="liveLinkButton"
                      onClick={() => openLiveTracking(order.id)}
                      disabled={liveLoadingId === order.id || order.status !== 'OUT_FOR_DELIVERY'}
                      title={order.status === 'OUT_FOR_DELIVERY'
                        ? 'Abrir link público de acompanhamento'
                        : 'Disponível quando o pedido sair para entrega'}
                    >
                      <ExternalLink size={15} />
                      {liveLoadingId === order.id ? 'Gerando...' : 'Abrir Live'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </article>
  )
}
