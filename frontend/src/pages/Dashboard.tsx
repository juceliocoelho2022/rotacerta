import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock3,
  MapPin,
  Navigation,
  Package,
  RefreshCcw,
  Route,
  Sparkles,
  Truck,
  Users,
  Zap
} from 'lucide-react'
import {
  api,
  type DispatchAssignment,
  type DriverRoute,
  type MonitoringDriver,
  type MonitoringOrder,
  type OperationsMonitoring
} from '../services/api'
import { StatusBadge } from '../components/StatusBadge'

const inProgressStatuses = new Set([
  'PICKING',
  'PACKING',
  'READY_FOR_SHIPMENT',
  'SHIPPED',
  'IN_TRANSIT',
  'OUT_FOR_DELIVERY'
])

function progressFor(status: MonitoringOrder['status']) {
  const progress: Record<MonitoringOrder['status'], number> = {
    ORDER_CREATED: 10,
    PAYMENT_APPROVED: 20,
    PICKING: 35,
    PACKING: 45,
    READY_FOR_SHIPMENT: 55,
    SHIPPED: 65,
    IN_TRANSIT: 75,
    OUT_FOR_DELIVERY: 90,
    DELIVERED: 100,
    DELIVERY_FAILED: 100,
    RETURNED: 100,
    CANCELLED: 100
  }
  return progress[status]
}

function formatNumber(value: number | null | undefined, digits = 1) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return Number(value).toLocaleString('pt-BR', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

function OperationsMap({
  monitoring,
  selectedOrder,
  route,
  onSelectOrder
}: {
  monitoring: OperationsMonitoring
  selectedOrder: MonitoringOrder | null
  route: DriverRoute | null
  onSelectOrder: (order: MonitoringOrder) => void
}) {
  const projection = useMemo(() => {
    const points = [
      ...monitoring.drivers.map(driver => ({ lat: driver.latitude, lon: driver.longitude })),
      ...monitoring.orders.map(order => ({ lat: order.latitude, lon: order.longitude }))
    ]

    const minLat = Math.min(...points.map(point => point.lat))
    const maxLat = Math.max(...points.map(point => point.lat))
    const minLon = Math.min(...points.map(point => point.lon))
    const maxLon = Math.max(...points.map(point => point.lon))
    const latSpan = Math.max(0.01, maxLat - minLat)
    const lonSpan = Math.max(0.01, maxLon - minLon)

    return (lat: number, lon: number) => ({
      x: 70 + ((lon - minLon) / lonSpan) * 860,
      y: 455 - ((lat - minLat) / latSpan) * 390
    })
  }, [monitoring])

  const selectedDriver = selectedOrder?.driverId
    ? monitoring.drivers.find(driver => driver.id === selectedOrder.driverId) ?? null
    : null

  const routePoints = useMemo(() => {
    if (!selectedDriver || !route) return []
    return [
      projection(selectedDriver.latitude, selectedDriver.longitude),
      ...route.stops.map(stop => projection(stop.latitude, stop.longitude))
    ]
  }, [projection, route, selectedDriver])

  const polyline = routePoints.map(point => `${point.x},${point.y}`).join(' ')

  return (
    <div className="opsMap">
      <div className="opsMapToolbar">
        <button className="mapMode active">Mapa</button>
        <button className="mapMode">Operação</button>
        <button className="mapMode">SLA</button>
        <span className="mapLive"><span className="healthDot" /> telemetria ativa</span>
      </div>

      <svg viewBox="0 0 1000 520" role="img" aria-label="Mapa operacional das entregas">
        <defs>
          <linearGradient id="mapBg" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#0a1b2b" />
            <stop offset="100%" stopColor="#07131f" />
          </linearGradient>
          <filter id="glow">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
          </filter>
        </defs>
        <rect width="1000" height="520" fill="url(#mapBg)" />

        {Array.from({ length: 14 }).map((_, index) => (
          <line
            key={`v-${index}`}
            x1={40 + index * 76}
            y1="0"
            x2={20 + index * 70}
            y2="520"
            className="mapRoad minor"
          />
        ))}
        {Array.from({ length: 10 }).map((_, index) => (
          <path
            key={`h-${index}`}
            d={`M 0 ${35 + index * 53} Q 260 ${15 + index * 57}, 500 ${45 + index * 48} T 1000 ${30 + index * 52}`}
            className="mapRoad"
          />
        ))}

        <path d="M80 465 C190 395 175 280 295 255 S460 285 520 190 S700 175 930 70" className="mapAvenue" />
        <path d="M60 110 C240 150 340 105 455 155 S665 275 940 250" className="mapAvenue secondary" />

        <text x="125" y="110" className="mapDistrict">PINHEIROS</text>
        <text x="355" y="140" className="mapDistrict">JARDINS</text>
        <text x="485" y="315" className="mapDistrict">ITAIM BIBI</text>
        <text x="650" y="155" className="mapDistrict">LIBERDADE</text>
        <text x="760" y="340" className="mapDistrict">IPIRANGA</text>

        {polyline && (
          <polyline
            points={polyline}
            fill="none"
            stroke="#1890ff"
            strokeWidth="5"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="routeLine"
          />
        )}

        {monitoring.drivers.map(driver => {
          const point = projection(driver.latitude, driver.longitude)
          const selected = driver.id === selectedDriver?.id
          return (
            <g key={`driver-${driver.id}`} transform={`translate(${point.x} ${point.y})`}>
              <circle r={selected ? 19 : 15} className={selected ? 'driverHalo selected' : 'driverHalo'} />
              <circle r="11" className="driverMarker" />
              <text y="4" textAnchor="middle" className="driverMarkerText">🚚</text>
              <text y="31" textAnchor="middle" className="mapLabel">{driver.name.split(' ')[0]}</text>
            </g>
          )
        })}

        {monitoring.orders.map(order => {
          const point = projection(order.latitude, order.longitude)
          const selected = selectedOrder?.id === order.id
          const delivered = order.status === 'DELIVERED'
          const failed = order.status === 'DELIVERY_FAILED'
          const markerClass = failed ? 'orderMarker failed' : delivered ? 'orderMarker delivered' : 'orderMarker'
          return (
            <g
              key={`order-${order.id}`}
              transform={`translate(${point.x} ${point.y})`}
              className="mapOrderGroup"
              onClick={() => onSelectOrder(order)}
            >
              {selected && <circle r="24" className="selectedOrderHalo" />}
              <circle r="15" className={markerClass} />
              <text y="5" textAnchor="middle" className="orderMarkerText">{order.priority}</text>
              <text y="33" textAnchor="middle" className="mapLabel">#{order.orderNumber}</text>
            </g>
          )
        })}
      </svg>

      <div className="mapLegend">
        <span><i className="legendDot driver" /> Motorista</span>
        <span><i className="legendDot progress" /> Em andamento</span>
        <span><i className="legendDot delivered" /> Entregue</span>
        <span><i className="legendDot failed" /> Falha</span>
        <span><i className="legendLine" /> Rota otimizada</span>
      </div>
    </div>
  )
}

export function Dashboard() {
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [selectedOrderId, setSelectedOrderId] = useState<number | null>(null)
  const [route, setRoute] = useState<DriverRoute | null>(null)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [driverFilter, setDriverFilter] = useState('ALL')
  const [assigning, setAssigning] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadMonitoring = useCallback(async () => {
    try {
      const response = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
      setMonitoring(response.data)
      setSelectedOrderId(current => current ?? response.data.orders[0]?.id ?? null)
      setError('')
    } catch {
      setError('Não foi possível carregar o monitoramento operacional.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadMonitoring()
    const timer = window.setInterval(loadMonitoring, 15000)
    return () => window.clearInterval(timer)
  }, [loadMonitoring])

  const selectedOrder = useMemo(
    () => monitoring?.orders.find(order => order.id === selectedOrderId) ?? monitoring?.orders[0] ?? null,
    [monitoring, selectedOrderId]
  )

  useEffect(() => {
    if (!selectedOrder?.driverId) {
      setRoute(null)
      return
    }

    api.get<DriverRoute>(`/api/dispatch/drivers/${selectedOrder.driverId}/route`)
      .then(response => setRoute(response.data))
      .catch(() => setRoute(null))
  }, [selectedOrder?.driverId, selectedOrder?.id])

  const filteredOrders = useMemo(() => {
    if (!monitoring) return []
    return monitoring.orders.filter(order => {
      const statusMatches = statusFilter === 'ALL'
        || (statusFilter === 'IN_PROGRESS' && inProgressStatuses.has(order.status))
        || order.status === statusFilter
      const driverMatches = driverFilter === 'ALL'
        || String(order.driverId ?? 'UNASSIGNED') === driverFilter
      return statusMatches && driverMatches
    })
  }, [driverFilter, monitoring, statusFilter])

  async function assignSelectedOrder() {
    if (!selectedOrder) return
    setAssigning(true)
    try {
      const response = await api.post<DispatchAssignment>(
        `/api/dispatch/orders/${selectedOrder.id}/assign`
      )
      await loadMonitoring()
      const routeResponse = await api.get<DriverRoute>(
        `/api/dispatch/drivers/${response.data.driverId}/route`
      )
      setRoute(routeResponse.data)
      setError('')
    } catch {
      setError('Não foi possível executar o Smart Dispatch para este pedido.')
    } finally {
      setAssigning(false)
    }
  }

  if (loading) {
    return <article className="panel empty"><Activity className="spin" /><h2>Carregando operação...</h2></article>
  }

  if (!monitoring) {
    return <article className="panel empty"><AlertTriangle /><h2>{error || 'Monitoramento indisponível'}</h2></article>
  }

  const selectedDriver = selectedOrder?.driverId
    ? monitoring.drivers.find(driver => driver.id === selectedOrder.driverId) ?? null
    : null

  const completed = monitoring.delivered + monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED').length
  const deliveredPct = monitoring.totalOrders === 0 ? 0 : (monitoring.delivered / monitoring.totalOrders) * 100
  const progressPct = monitoring.totalOrders === 0 ? 0 : (monitoring.inProgress / monitoring.totalOrders) * 100
  const failedPct = monitoring.totalOrders === 0 ? 0 : (monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED').length / monitoring.totalOrders) * 100

  const metrics = [
    { label: 'Entregas Hoje', value: monitoring.totalOrders, meta: 'pedidos na operação', icon: Package, tone: 'blue' },
    { label: 'Em Andamento', value: monitoring.inProgress, meta: `${formatNumber(progressPct)}% do total`, icon: Route, tone: 'indigo' },
    { label: 'Entregues', value: monitoring.delivered, meta: `${formatNumber(deliveredPct)}% do total`, icon: CheckCircle2, tone: 'green' },
    { label: 'Atrasadas', value: monitoring.delayed, meta: 'ETA acima do SLA', icon: Clock3, tone: 'red' },
    { label: 'Motoristas Ativos', value: monitoring.activeDrivers, meta: `${monitoring.drivers.length} cadastrados`, icon: Users, tone: 'amber' },
    { label: 'Taxa de Sucesso', value: `${formatNumber(monitoring.successRate)}%`, meta: completed ? 'entregas finalizadas' : 'aguardando histórico', icon: Zap, tone: 'green' }
  ]

  const alerts = [
    ...(monitoring.delayed > 0 ? [{ tone: 'danger', title: 'Entrega com risco de atraso', text: `${monitoring.delayed} atribuição(ões) acima do SLA calculado.` }] : []),
    ...monitoring.orders.filter(order => !order.driverId && inProgressStatuses.has(order.status)).slice(0, 2).map(order => ({
      tone: 'warning',
      title: 'Pedido sem motorista',
      text: `#${order.orderNumber} aguarda Smart Dispatch.`
    })),
    ...monitoring.orders.filter(order => order.priority >= 5).slice(0, 1).map(order => ({
      tone: 'info',
      title: 'Prioridade máxima',
      text: `#${order.orderNumber} possui prioridade ${order.priority}/5.`
    }))
  ]

  return (
    <div className="opsDashboard">
      <section className="opsTitleBar">
        <div>
          <div className="opsTitleLine">
            <h1>Monitoramento de Entregas</h1>
            <span className="liveBadge"><span className="healthDot" /> Ao vivo</span>
          </div>
          <p>Acompanhe rotas, motoristas, ETA, SLA e decisões do Smart Dispatch.</p>
        </div>
        <button className="refreshButton" onClick={loadMonitoring}>
          <RefreshCcw size={16} /> Atualizar
        </button>
      </section>

      {error && <div className="opsError"><AlertTriangle size={16} /> {error}</div>}

      <section className="opsMetrics">
        {metrics.map(({ label, value, meta, icon: Icon, tone }) => (
          <article className="opsMetric" key={label}>
            <div className={`opsMetricIcon ${tone}`}><Icon size={22} /></div>
            <div>
              <span>{label}</span>
              <strong>{value}</strong>
              <small>{meta}</small>
            </div>
          </article>
        ))}
      </section>

      <section className="opsFilters">
        <select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}>
          <option value="ALL">Todos os status</option>
          <option value="IN_PROGRESS">Em andamento</option>
          <option value="OUT_FOR_DELIVERY">Saiu para entrega</option>
          <option value="DELIVERED">Entregues</option>
          <option value="DELIVERY_FAILED">Falhas</option>
        </select>
        <select value={driverFilter} onChange={event => setDriverFilter(event.target.value)}>
          <option value="ALL">Todos os motoristas</option>
          <option value="UNASSIGNED">Sem motorista</option>
          {monitoring.drivers.map(driver => (
            <option key={driver.id} value={driver.id}>{driver.name}</option>
          ))}
        </select>
        <span className="filterResult">{filteredOrders.length} pedido(s) no filtro atual</span>
      </section>

      <section className="opsMainGrid">
        <article className="opsMapPanel">
          <OperationsMap
            monitoring={{ ...monitoring, orders: filteredOrders.length ? filteredOrders : monitoring.orders }}
            selectedOrder={selectedOrder}
            route={route}
            onSelectOrder={order => setSelectedOrderId(order.id)}
          />
        </article>

        <aside className="deliveryInspector">
          {selectedOrder ? (
            <>
              <div className="inspectorHeader">
                <div>
                  <span>Entrega selecionada</span>
                  <h2>#{selectedOrder.orderNumber}</h2>
                </div>
                <StatusBadge status={selectedOrder.status} />
              </div>

              <div className="inspectorSection">
                <small>Cliente</small>
                <strong>{selectedOrder.customerName}</strong>
              </div>

              <div className="inspectorSection">
                <small>Destino GPS</small>
                <div className="inlineInfo"><MapPin size={16} /> {selectedOrder.latitude.toFixed(5)}, {selectedOrder.longitude.toFixed(5)}</div>
                <div className="miniGrid">
                  <span><small>Prioridade</small><b>{selectedOrder.priority}/5</b></span>
                  <span><small>SLA</small><b>{selectedOrder.slaMinutes} min</b></span>
                </div>
              </div>

              <div className="inspectorSection">
                <small>Progresso da entrega</small>
                <div className="progressRow"><b>{progressFor(selectedOrder.status)}%</b><span>{selectedOrder.status.replaceAll('_', ' ')}</span></div>
                <div className="progressTrack"><i style={{ width: `${progressFor(selectedOrder.status)}%` }} /></div>
              </div>

              <div className="smartDispatchBox">
                <div className="smartDispatchTitle"><Sparkles size={17} /><b>Smart Dispatch</b></div>
                {selectedDriver ? (
                  <>
                    <div className="driverSelected">
                      <div className="driverAvatar">{selectedDriver.name.charAt(0)}</div>
                      <div><small>Motorista selecionado</small><strong>{selectedDriver.name}</strong></div>
                      <span className="smartScore">{formatNumber(selectedOrder.score, 2)}</span>
                    </div>
                    <div className="dispatchStats">
                      <span><small>Distância</small><b>{formatNumber(selectedOrder.distanceKm, 2)} km</b></span>
                      <span><small>ETA</small><b>{selectedOrder.etaMinutes ?? '—'} min</b></span>
                      <span><small>Carga</small><b>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</b></span>
                    </div>
                    <p className="dispatchReason">Escolha baseada em distância, carga, pressão de SLA e prioridade do pedido.</p>
                  </>
                ) : (
                  <>
                    <p className="dispatchReason">Este pedido ainda não possui motorista. O motor pode comparar todos os profissionais disponíveis e escolher o menor score logístico.</p>
                    <button className="dispatchButton" onClick={assignSelectedOrder} disabled={assigning}>
                      <Sparkles size={16} /> {assigning ? 'Calculando...' : 'Selecionar melhor motorista'}
                    </button>
                  </>
                )}
              </div>

              {route && (
                <div className="routeSummary">
                  <Navigation size={17} />
                  <div><small>Rota otimizada</small><strong>{route.totalStops} paradas • {formatNumber(route.totalDistanceKm, 2)} km • {route.totalEtaMinutes} min</strong></div>
                </div>
              )}
            </>
          ) : <div className="emptyInspector">Selecione uma entrega no mapa.</div>}
        </aside>
      </section>

      <section className="opsBottomGrid">
        <article className="opsCard recentDeliveries">
          <div className="opsCardHeader"><div><h3>Entregas Recentes</h3><p>Pedidos e alocação atual</p></div><Truck size={18} /></div>
          <div className="recentList">
            {filteredOrders.slice(0, 6).map(order => (
              <button key={order.id} className={order.id === selectedOrder?.id ? 'recentRow selected' : 'recentRow'} onClick={() => setSelectedOrderId(order.id)}>
                <span className="recentCode">#{order.orderNumber}</span>
                <span className="recentCustomer">{order.customerName}</span>
                <StatusBadge status={order.status} />
                <span className="recentDriver">{order.driverName ?? 'Sem motorista'}</span>
              </button>
            ))}
          </div>
        </article>

        <article className="opsCard performanceCard">
          <div className="opsCardHeader"><div><h3>Eficiência da Operação</h3><p>Indicadores do motor logístico</p></div><Activity size={18} /></div>
          <svg viewBox="0 0 500 210" className="performanceChart">
            <defs>
              <linearGradient id="chartFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#31d26c" stopOpacity=".32" />
                <stop offset="100%" stopColor="#31d26c" stopOpacity="0" />
              </linearGradient>
            </defs>
            {[35, 75, 115, 155].map(y => <line key={y} x1="30" x2="480" y1={y} y2={y} className="chartGrid" />)}
            <path d="M30 180 C95 167 115 142 165 135 S240 88 285 98 S355 58 395 67 S450 35 480 30 L480 195 L30 195 Z" fill="url(#chartFill)" />
            <path d="M30 180 C95 167 115 142 165 135 S240 88 285 98 S355 58 395 67 S450 35 480 30" className="chartActual" />
            <path d="M30 170 C95 140 135 130 180 100 S265 92 315 58 S410 46 480 18" className="chartPlanned" />
          </svg>
          <div className="chartLegend"><span><i className="legendDot delivered" /> operação realizada</span><span><i className="legendDash" /> capacidade planejada</span></div>
        </article>

        <article className="opsCard statusCard">
          <div className="opsCardHeader"><div><h3>Status das Entregas</h3><p>Distribuição operacional</p></div></div>
          <div className="donutWrap">
            <div
              className="statusDonut"
              style={{
                background: `conic-gradient(#33c86b 0 ${deliveredPct}%, #1689ff ${deliveredPct}% ${deliveredPct + progressPct}%, #ef5350 ${deliveredPct + progressPct}% ${deliveredPct + progressPct + failedPct}%, #53677b ${deliveredPct + progressPct + failedPct}% 100%)`
              }}
            >
              <div><small>Total</small><strong>{monitoring.totalOrders}</strong></div>
            </div>
            <div className="statusLegend">
              <span><i className="legendDot progress" /> Em andamento <b>{monitoring.inProgress}</b></span>
              <span><i className="legendDot delivered" /> Entregues <b>{monitoring.delivered}</b></span>
              <span><i className="legendDot failed" /> Falhas <b>{monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED').length}</b></span>
              <span><i className="legendDot neutral" /> Outros <b>{Math.max(0, monitoring.totalOrders - monitoring.inProgress - monitoring.delivered - monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED').length)}</b></span>
            </div>
          </div>
        </article>

        <article className="opsCard alertsCard">
          <div className="opsCardHeader"><div><h3>Alertas e Ocorrências</h3><p>Atenção operacional</p></div><AlertTriangle size={18} /></div>
          <div className="alertList">
            {alerts.length ? alerts.map((alert, index) => (
              <div className={`alertItem ${alert.tone}`} key={`${alert.title}-${index}`}>
                <AlertTriangle size={17} />
                <div><strong>{alert.title}</strong><span>{alert.text}</span></div>
              </div>
            )) : (
              <div className="alertItem success"><CheckCircle2 size={17} /><div><strong>Operação normal</strong><span>Nenhum alerta crítico no momento.</span></div></div>
            )}
          </div>
        </article>
      </section>
    </div>
  )
}
