import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  Clock3,
  Eye,
  MapPinned,
  MoreHorizontal,
  Package,
  RefreshCcw,
  Route,
  Search,
  Sparkles,
  Truck,
  UserRoundCheck
} from 'lucide-react'
import {
  api,
  type DispatchAssignment,
  type MonitoringOrder,
  type OperationsMonitoring,
  type RouteOptimization
} from '../services/api'
import { StatusBadge } from '../components/StatusBadge'

const progressByStatus: Record<MonitoringOrder['status'], number> = {
  ORDER_CREATED: 10,
  PAYMENT_APPROVED: 20,
  PICKING: 30,
  PACKING: 40,
  READY_FOR_SHIPMENT: 50,
  SHIPPED: 60,
  IN_TRANSIT: 72,
  OUT_FOR_DELIVERY: 88,
  DELIVERED: 100,
  DELIVERY_FAILED: 100,
  RETURNED: 100,
  CANCELLED: 100
}

function riskClass(order: MonitoringOrder) {
  if (order.riskPercent >= 90) return 'critical'
  if (order.riskPercent >= 70) return 'high'
  if (order.riskPercent >= 50) return 'medium'
  return 'low'
}

export function Deliveries() {
  const navigate = useNavigate()
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('ALL')
  const [driver, setDriver] = useState('ALL')
  const [region, setRegion] = useState('ALL')
  const [priority, setPriority] = useState('ALL')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [optimization, setOptimization] = useState<RouteOptimization | null>(null)

  const load = useCallback(async () => {
    const response = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
    setMonitoring(response.data)
    setSelectedId(current => current ?? response.data.orders[0]?.id ?? null)
  }, [])

  useEffect(() => {
    load().catch(() => setMessage('Não foi possível carregar as entregas.'))
    const timer = window.setInterval(() => load().catch(() => undefined), 15000)
    return () => window.clearInterval(timer)
  }, [load])

  const selected = useMemo(
    () => monitoring?.orders.find(order => order.id === selectedId) ?? null,
    [monitoring, selectedId]
  )

  const regions = useMemo(
    () => [...new Set(monitoring?.orders.map(order => order.region) ?? [])].sort(),
    [monitoring]
  )

  const filtered = useMemo(() => {
    if (!monitoring) return []
    const term = search.trim().toLowerCase()
    return monitoring.orders.filter(order => {
      const matchesSearch = !term
        || order.orderNumber.toLowerCase().includes(term)
        || order.customerName.toLowerCase().includes(term)
        || order.destinationLabel.toLowerCase().includes(term)
      const matchesStatus = status === 'ALL' || order.status === status
      const matchesDriver = driver === 'ALL' || String(order.driverId ?? 'UNASSIGNED') === driver
      const matchesRegion = region === 'ALL' || order.region === region
      const matchesPriority = priority === 'ALL' || String(order.priority) === priority
      return matchesSearch && matchesStatus && matchesDriver && matchesRegion && matchesPriority
    })
  }, [driver, monitoring, priority, region, search, status])

  async function assignDriver() {
    if (!selected) return
    setBusy(true)
    try {
      const response = await api.post<DispatchAssignment>(`/api/dispatch/orders/${selected.id}/assign`)
      setMessage(`Smart Dispatch selecionou ${response.data.driverName} com ETA de ${response.data.etaMinutes} min.`)
      await load()
    } catch {
      setMessage('Não foi possível selecionar um motorista para esta entrega.')
    } finally {
      setBusy(false)
    }
  }

  async function recalculateRoute() {
    if (!selected?.driverId) return
    setBusy(true)
    try {
      const response = await api.post<RouteOptimization>(`/api/dispatch/drivers/${selected.driverId}/route/optimize`)
      setOptimization(response.data)
      setMessage(`Rota recalculada: economia potencial de ${response.data.savedDistanceKm.toFixed(2)} km e ${response.data.savedMinutes} min.`)
    } catch {
      setMessage('Não foi possível recalcular a rota do motorista.')
    } finally {
      setBusy(false)
    }
  }

  if (!monitoring) {
    return <article className="panel empty"><RefreshCcw className="spin" /><h2>Carregando entregas...</h2></article>
  }

  const waitingPickup = monitoring.orders.filter(order => order.status === 'READY_FOR_SHIPMENT').length
  const occurrences = monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED' || order.riskPercent >= 90).length

  const cards = [
    ['Total hoje', monitoring.totalOrders, Package],
    ['Em andamento', monitoring.inProgress, Truck],
    ['Aguardando coleta', waitingPickup, Clock3],
    ['Entregues', monitoring.delivered, UserRoundCheck],
    ['Atrasadas', monitoring.delayed, AlertTriangle],
    ['Com ocorrência', occurrences, AlertTriangle]
  ] as const

  return (
    <div className="deliveriesPage">
      <section className="deliveriesHeader">
        <div><h1>Entregas</h1><p>Gerencie, acompanhe e intervenha nas entregas da operação.</p></div>
        <button className="refreshButton" onClick={() => load()}><RefreshCcw size={16} /> Atualizar</button>
      </section>

      {message && <div className="deliveryNotice">{message}</div>}

      <section className="deliveryMetrics">
        {cards.map(([label, value, Icon]) => (
          <article key={label}><Icon size={20} /><div><span>{label}</span><strong>{value}</strong></div></article>
        ))}
      </section>

      <section className="deliveryToolbar">
        <label className="deliverySearch"><Search size={16} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar pedido, cliente ou destino..." /></label>
        <select value={status} onChange={event => setStatus(event.target.value)}>
          <option value="ALL">Status</option>
          <option value="READY_FOR_SHIPMENT">Aguardando coleta</option>
          <option value="IN_TRANSIT">Em transporte</option>
          <option value="OUT_FOR_DELIVERY">Em rota</option>
          <option value="DELIVERED">Entregue</option>
          <option value="DELIVERY_FAILED">Falha</option>
        </select>
        <select value={driver} onChange={event => setDriver(event.target.value)}>
          <option value="ALL">Motorista</option><option value="UNASSIGNED">Sem motorista</option>
          {monitoring.drivers.map(item => <option value={item.id} key={item.id}>{item.name}</option>)}
        </select>
        <select value={region} onChange={event => setRegion(event.target.value)}>
          <option value="ALL">Região</option>{regions.map(item => <option value={item} key={item}>{item}</option>)}
        </select>
        <select value={priority} onChange={event => setPriority(event.target.value)}>
          <option value="ALL">Prioridade</option>{[5,4,3,2,1].map(item => <option value={item} key={item}>P{item}</option>)}
        </select>
        <button className="dispatchButton" onClick={() => navigate('/orders')}>+ Nova entrega</button>
      </section>

      <section className="deliveryWorkspace">
        <article className="deliveryTablePanel">
          <div className="deliveryTableHeader"><strong>{filtered.length} entregas</strong><span>Atualização automática a cada 15 s</span></div>
          <div className="tableWrap">
            <table className="deliveryTable">
              <thead><tr><th>Pedido</th><th>Cliente</th><th>Motorista</th><th>Destino</th><th>Status</th><th>ETA</th><th>Progresso</th><th>Risco</th><th>Ações</th></tr></thead>
              <tbody>
                {filtered.map(order => (
                  <tr key={order.id} className={selectedId === order.id ? 'selected' : ''} onClick={() => { setSelectedId(order.id); setOptimization(null) }}>
                    <td className="mono">#{order.orderNumber}</td>
                    <td>{order.customerName}</td>
                    <td>{order.driverName ?? <span className="muted">Sem motorista</span>}</td>
                    <td><strong>{order.region}</strong><small>{order.destinationLabel}</small></td>
                    <td><StatusBadge status={order.status} /></td>
                    <td>{order.etaMinutes == null ? '—' : `${order.etaMinutes} min`}</td>
                    <td><div className="tableProgress"><span><i style={{ width: `${progressByStatus[order.status]}%` }} /></span><b>{progressByStatus[order.status]}%</b></div></td>
                    <td><span className={`riskBadge ${riskClass(order)}`}>{order.riskPercent}%</span></td>
                    <td><button className="rowAction" onClick={event => { event.stopPropagation(); setSelectedId(order.id) }}><Eye size={15} /></button><button className="rowAction"><MoreHorizontal size={15} /></button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>

        {selected && (
          <aside className="deliveryDrawer">
            <div className="drawerTop"><div><span>Pedido</span><h2>#{selected.orderNumber}</h2></div><StatusBadge status={selected.status} /></div>
            <div className={`riskPanel ${riskClass(selected)}`}><AlertTriangle size={18} /><div><strong>Risco de atraso: {selected.riskPercent}%</strong><span>{selected.riskReason}</span></div></div>

            <dl className="deliveryDetails">
              <div><dt>Cliente</dt><dd>{selected.customerName}</dd></div>
              <div><dt>Motorista</dt><dd>{selected.driverName ?? 'Não atribuído'}</dd></div>
              <div><dt>Destino</dt><dd>{selected.destinationLabel}<small>{selected.region}</small></dd></div>
              <div><dt>ETA</dt><dd>{selected.etaMinutes == null ? 'Aguardando despacho' : `${selected.etaMinutes} minutos`}</dd></div>
              <div><dt>SLA</dt><dd>{selected.slaMinutes} minutos</dd></div>
              <div><dt>Prioridade</dt><dd>P{selected.priority}</dd></div>
            </dl>

            <div className="drawerTimeline">
              <h3>Linha do tempo</h3>
              {['Pedido confirmado','Separação concluída','Pronto para expedição','Em transporte','Saiu para entrega','Entregue'].map((item,index) => {
                const threshold = [10,30,50,72,88,100][index]
                const done = progressByStatus[selected.status] >= threshold
                return <div key={item} className={done ? 'done' : ''}><i /> <span>{item}</span></div>
              })}
            </div>

            {optimization && (
              <div className="optimizationResult">
                <Sparkles size={17} />
                <div><strong>Otimização calculada</strong><span>{optimization.currentDistanceKm.toFixed(2)} km → {optimization.optimizedDistanceKm.toFixed(2)} km</span><small>Economia: {optimization.savedDistanceKm.toFixed(2)} km • {optimization.savedMinutes} min</small></div>
              </div>
            )}

            <div className="drawerActions">
              <button onClick={() => navigate('/routes')}><MapPinned size={16} /> Ver no mapa</button>
              {selected.driverId ? <button onClick={recalculateRoute} disabled={busy}><Route size={16} /> Recalcular rota</button> : <button onClick={assignDriver} disabled={busy}><Sparkles size={16} /> Selecionar motorista</button>}
            </div>
          </aside>
        )}
      </section>
    </div>
  )
}
