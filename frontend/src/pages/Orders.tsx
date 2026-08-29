import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  CalendarDays,
  Clock3,
  ExternalLink,
  Eye,
  PackageCheck,
  Plus,
  RefreshCcw,
  Search,
  ShoppingCart,
  Truck
} from 'lucide-react'
import { OrderCreateModal } from '../components/OrderCreateModal'
import { StatusBadge } from '../components/StatusBadge'
import {
  api,
  type DeliveryType,
  type Order,
  type OrderDetail,
  type OrderPriority
} from '../services/api'

const terminalStatuses = new Set(['DELIVERED', 'DELIVERY_FAILED', 'RETURNED', 'CANCELLED'])

const priorityLabel: Record<OrderPriority, string> = {
  NORMAL: 'Normal',
  HIGH: 'Alta',
  URGENT: 'Urgente'
}

const deliveryTypeLabel: Record<DeliveryType, string> = {
  STANDARD: 'Standard',
  EXPRESS: 'Express',
  SAME_DAY: 'Same-Day',
  SCHEDULED: 'Agendada'
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function formatDate(value: string | null | undefined) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(new Date(`${value}T12:00:00`))
}

export function Orders() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<Order[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [detail, setDetail] = useState<OrderDetail | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [priorityFilter, setPriorityFilter] = useState('ALL')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [detailLoading, setDetailLoading] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [liveLoadingId, setLiveLoadingId] = useState<number | null>(null)
  const [message, setMessage] = useState('')

  const loadOrders = useCallback(async () => {
    setLoading(true)
    try {
      const response = await api.get<Order[]>('/api/orders')
      setOrders(response.data)
      setSelectedId(current => current ?? response.data[0]?.id ?? null)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar os pedidos.')
    } finally {
      setLoading(false)
    }
  }, [])

  const loadDetail = useCallback(async (id: number) => {
    setDetailLoading(true)
    try {
      const response = await api.get<OrderDetail>(`/api/orders/${id}/detail`)
      setDetail(response.data)
    } finally {
      setDetailLoading(false)
    }
  }, [])

  useEffect(() => { loadOrders() }, [loadOrders])

  useEffect(() => {
    if (!selectedId) {
      setDetail(null)
      return
    }
    loadDetail(selectedId).catch(() => setMessage('Não foi possível carregar os detalhes do pedido.'))
  }, [loadDetail, selectedId])

  async function handleCreated(orderId: number) {
    setCreateOpen(false)
    await loadOrders()
    setSelectedId(orderId)
    await loadDetail(orderId)
    setMessage('Pedido criado com sucesso e registrado como ORDER_CREATED.')
  }

  async function openLiveTracking(id: number) {
    setLiveLoadingId(id)
    try {
      const response = await api.post<{ publicUrl: string }>(`/api/deliveries/${id}/live-link`)
      window.open(response.data.publicUrl, '_blank', 'noopener,noreferrer')
    } catch {
      setMessage('Não foi possível gerar o link RotaCerta Live para este pedido.')
    } finally {
      setLiveLoadingId(null)
    }
  }

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    return orders.filter(order => {
      const matchesSearch = !term || order.orderNumber.toLowerCase().includes(term) || order.customerName.toLowerCase().includes(term) || order.trackingCode.toLowerCase().includes(term)
      const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter
      const matchesPriority = priorityFilter === 'ALL' || order.priority === priorityFilter
      const matchesType = typeFilter === 'ALL' || order.deliveryType === typeFilter
      return matchesSearch && matchesStatus && matchesPriority && matchesType
    })
  }, [orders, priorityFilter, search, statusFilter, typeFilter])

  const waiting = orders.filter(order => ['ORDER_CREATED', 'PAYMENT_APPROVED'].includes(order.status)).length
  const preparation = orders.filter(order => ['PICKING', 'PACKING', 'READY_FOR_SHIPMENT'].includes(order.status)).length
  const inDelivery = orders.filter(order => ['SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(order.status)).length
  const completed = orders.filter(order => order.status === 'DELIVERED').length
  const cancelled = orders.filter(order => order.status === 'CANCELLED').length
  const attention = orders.filter(order => order.priority !== 'NORMAL' && !terminalStatuses.has(order.status)).length

  const metrics = [
    ['Pedidos', orders.length, ShoppingCart],
    ['Aguardando', waiting, Clock3],
    ['Preparação', preparation, PackageCheck],
    ['Em entrega', inDelivery, Truck],
    ['Concluídos', completed, PackageCheck],
    ['Cancelados', cancelled, AlertTriangle],
    ['Alta prioridade', attention, AlertTriangle]
  ] as const

  return (
    <div className="ordersPage">
      <section className="ordersHeader">
        <div><h1>Pedidos</h1><p>Entrada comercial, preparação logística e acompanhamento do ciclo de cada pedido.</p></div>
        <div className="ordersHeaderActions"><button onClick={loadOrders}><RefreshCcw size={16}/> Atualizar</button><button className="primary" onClick={() => setCreateOpen(true)}><Plus size={16}/> Novo pedido</button></div>
      </section>

      {message && <div className="ordersNotice">{message}</div>}

      <section className="orderMetrics">
        {metrics.map(([label, value, Icon]) => <article key={label}><span><Icon size={19}/></span><div><small>{label}</small><strong>{value}</strong></div></article>)}
      </section>

      {attention > 0 && (
        <section className="ordersAttention"><AlertTriangle size={18}/><div><strong>{attention} pedido(s) de alta prioridade exigem atenção</strong><span>Use prioridade, modalidade e janela de entrega para orientar o processamento antes do Smart Dispatch.</span></div></section>
      )}

      <section className="ordersToolbar">
        <label className="ordersSearch"><Search size={16}/><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar pedido, cliente ou rastreio..." /></label>
        <select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}><option value="ALL">Status</option><option value="ORDER_CREATED">Criado</option><option value="PICKING">Separação</option><option value="PACKING">Embalagem</option><option value="READY_FOR_SHIPMENT">Pronto</option><option value="IN_TRANSIT">Em trânsito</option><option value="OUT_FOR_DELIVERY">Saiu para entrega</option><option value="DELIVERED">Entregue</option><option value="CANCELLED">Cancelado</option></select>
        <select value={priorityFilter} onChange={event => setPriorityFilter(event.target.value)}><option value="ALL">Prioridade</option><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select>
        <select value={typeFilter} onChange={event => setTypeFilter(event.target.value)}><option value="ALL">Modalidade</option><option value="STANDARD">Standard</option><option value="EXPRESS">Express</option><option value="SAME_DAY">Same-Day</option><option value="SCHEDULED">Agendada</option></select>
      </section>

      <section className="ordersWorkspace">
        <article className="ordersTablePanel">
          <div className="ordersTableHead"><strong>{filtered.length} pedido(s)</strong><span>Dados reais da API Spring Boot</span></div>
          <div className="tableWrap">
            <table className="ordersTable">
              <thead><tr><th>Pedido</th><th>Cliente</th><th>Criado</th><th>Prioridade</th><th>Status</th><th>Modalidade</th><th>Valor</th><th>Ações</th></tr></thead>
              <tbody>
                {filtered.map(order => (
                  <tr className={selectedId === order.id ? 'selected' : ''} key={order.id} onClick={() => setSelectedId(order.id)}>
                    <td><strong>#{order.orderNumber}</strong><small>{order.trackingCode}</small></td>
                    <td>{order.customerName}</td>
                    <td>{formatDateTime(order.createdAt)}</td>
                    <td><span className={`orderPriority ${order.priority.toLowerCase()}`}>{priorityLabel[order.priority]}</span></td>
                    <td><StatusBadge status={order.status}/></td>
                    <td><span className="orderType">{deliveryTypeLabel[order.deliveryType]}</span></td>
                    <td><strong>{formatMoney(order.total)}</strong></td>
                    <td><button className="orderRowButton" type="button" onClick={event => { event.stopPropagation(); setSelectedId(order.id) }}><Eye size={15}/></button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {loading && <div className="ordersLoading"><RefreshCcw className="spin" size={16}/> Carregando pedidos...</div>}
        </article>

        <aside className="orderDrawer">
          {detail ? (
            <>
              <header className="orderDrawerHeader">
                <div><small>PEDIDO</small><h2>#{detail.orderNumber}</h2><StatusBadge status={detail.status}/></div>
                <div className="orderDrawerBadges"><span className={`orderPriority ${detail.priority.toLowerCase()}`}>{priorityLabel[detail.priority]}</span><span className="orderType">{deliveryTypeLabel[detail.deliveryType]}</span></div>
              </header>

              {detailLoading ? <div className="orderDrawerLoading">Atualizando detalhes...</div> : (
                <div className="orderDrawerBody">
                  <section className="orderCustomerSummary"><small>Cliente</small><strong>{detail.customerName}</strong><span>Criado em {formatDateTime(detail.createdAt)}</span></section>

                  <section className="orderItemsPanel">
                    <div className="orderSectionTitle"><strong>Itens</strong><span>{detail.totalPackages} volume(s)</span></div>
                    {detail.items.length ? detail.items.map(item => <div className="orderDetailItem" key={item.id}><div><strong>{item.productName}</strong><span>{item.quantity} × {formatMoney(item.unitPrice)}</span></div><b>{formatMoney(item.subtotal)}</b></div>) : <div className="orderEmptySmall">Pedido legado sem itens detalhados.</div>}
                    <div className="orderLogisticsStats"><span><small>Peso</small><strong>{Number(detail.totalWeightKg).toFixed(3)} kg</strong></span><span><small>Volume</small><strong>{Number(detail.totalVolumeM3).toFixed(4)} m³</strong></span><span><small>Total</small><strong>{formatMoney(detail.total)}</strong></span></div>
                  </section>

                  <section className="orderDeliveryPanel">
                    <div className="orderSectionTitle"><strong>Entrega</strong><CalendarDays size={15}/></div>
                    {detail.delivery ? <><div className="orderDestination"><strong>{detail.delivery.addressLabel}</strong><span>{detail.delivery.street}, {detail.delivery.number}{detail.delivery.complement ? ` • ${detail.delivery.complement}` : ''}</span><small>{detail.delivery.district ? `${detail.delivery.district} • ` : ''}{detail.delivery.city}/{detail.delivery.state} {detail.delivery.zipCode ?? ''}</small></div><div className="orderDeliveryMeta"><span><small>Data</small><strong>{formatDate(detail.delivery.deliveryDate)}</strong></span><span><small>Janela</small><strong>{detail.delivery.windowStart && detail.delivery.windowEnd ? `${detail.delivery.windowStart.slice(0, 5)} — ${detail.delivery.windowEnd.slice(0, 5)}` : 'Sem restrição'}</strong></span></div>{detail.delivery.instructions && <div className="orderInstruction"><small>Instruções</small><span>{detail.delivery.instructions}</span></div>}</> : <div className="orderEmptySmall">Sem snapshot de entrega.</div>}
                  </section>

                  <section className="orderTrackingPanel"><small>Código de rastreio</small><strong>{detail.trackingCode}</strong><div><button onClick={() => navigate('/tracking')}>Acompanhar</button><button disabled={detail.status !== 'OUT_FOR_DELIVERY' || liveLoadingId === detail.id} onClick={() => openLiveTracking(detail.id)}><ExternalLink size={14}/>{liveLoadingId === detail.id ? 'Gerando...' : 'RotaCerta Live'}</button></div></section>
                </div>
              )}
            </>
          ) : <div className="orderDrawerEmpty"><ShoppingCart size={28}/><strong>Selecione um pedido</strong><span>Itens, destino e condições da entrega aparecerão aqui.</span></div>}
        </aside>
      </section>

      {createOpen && <OrderCreateModal onClose={() => setCreateOpen(false)} onCreated={handleCreated}/>} 
    </div>
  )
}