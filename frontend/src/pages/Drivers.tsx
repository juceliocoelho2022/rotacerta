import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Activity,
  AlertTriangle,
  CarFront,
  CheckCircle2,
  Clock3,
  Eye,
  Gauge,
  MapPin,
  Navigation,
  PackageCheck,
  Power,
  RefreshCcw,
  Search,
  Star,
  Truck,
  UserPlus,
  UsersRound
} from 'lucide-react'
import { DriverCreateModal } from '../components/DriverCreateModal'
import { api, type DriverRoute, type MonitoringDriver, type MonitoringOrder, type OperationsMonitoring } from '../services/api'

const ACTIVE_STATUSES = new Set(['READY_FOR_SHIPMENT', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'])

type DriverOperationalStatus = 'AVAILABLE' | 'ON_ROUTE' | 'ATTENTION' | 'UNAVAILABLE'
type PerformanceFilter = 'ALL' | 'HIGH' | 'MEDIUM' | 'ATTENTION'

type MapPoint = {
  id: string
  x: number
  y: number
  label: string
  kind: 'driver' | 'stop'
  position?: number
}

type RawMapPoint = {
  id: string
  latitude: number
  longitude: number
  label: string
  kind: 'driver' | 'stop'
  position?: number
}

function operationalStatus(driver: MonitoringDriver, orders: MonitoringOrder[]): DriverOperationalStatus {
  const assigned = orders.filter(order => order.driverId === driver.id)
  const needsAttention = assigned.some(order => ACTIVE_STATUSES.has(order.status) && order.riskPercent >= 85)
  const hasActiveRoute = assigned.some(order => ACTIVE_STATUSES.has(order.status))

  if (needsAttention) return 'ATTENTION'
  if (!driver.available) return 'UNAVAILABLE'
  if (hasActiveRoute) return 'ON_ROUTE'
  return 'AVAILABLE'
}

function statusLabel(status: DriverOperationalStatus) {
  return {
    AVAILABLE: 'Disponível',
    ON_ROUTE: 'Em rota',
    ATTENTION: 'Atenção',
    UNAVAILABLE: 'Indisponível'
  }[status]
}

function operationalScore(driver: MonitoringDriver, orders: MonitoringOrder[]) {
  const activeOrders = orders.filter(order => order.driverId === driver.id && ACTIVE_STATUSES.has(order.status))
  const averageRisk = activeOrders.length
    ? activeOrders.reduce((sum, order) => sum + order.riskPercent, 0) / activeOrders.length
    : 0
  const loadRatio = driver.maxCapacity > 0 ? driver.currentLoad / driver.maxCapacity : 1

  return Math.max(0, Math.min(100, Math.round(100 - averageRisk * 0.45 - loadRatio * 20)))
}

function formatCoordinate(value: number) {
  return Number(value).toFixed(6)
}

function performanceMatches(score: number, filter: PerformanceFilter) {
  if (filter === 'ALL') return true
  if (filter === 'HIGH') return score >= 90
  if (filter === 'MEDIUM') return score >= 75 && score < 90
  return score < 75
}

function createMapPoints(driver: MonitoringDriver | null, route: DriverRoute | null): MapPoint[] {
  if (!driver) return []

  const raw: RawMapPoint[] = [
    { id: `driver-${driver.id}`, latitude: driver.latitude, longitude: driver.longitude, label: driver.name, kind: 'driver' },
    ...(route?.stops ?? []).map(stop => ({
      id: `stop-${stop.orderId}`,
      latitude: stop.latitude,
      longitude: stop.longitude,
      label: stop.orderNumber,
      kind: 'stop' as const,
      position: stop.position
    }))
  ]

  const lats = raw.map(point => point.latitude)
  const lngs = raw.map(point => point.longitude)
  const minLat = Math.min(...lats)
  const maxLat = Math.max(...lats)
  const minLng = Math.min(...lngs)
  const maxLng = Math.max(...lngs)
  const latRange = Math.max(maxLat - minLat, 0.01)
  const lngRange = Math.max(maxLng - minLng, 0.01)

  return raw.map(point => ({
    id: point.id,
    x: 8 + ((point.longitude - minLng) / lngRange) * 84,
    y: 46 - ((point.latitude - minLat) / latRange) * 38,
    label: point.label,
    kind: point.kind,
    position: point.position
  }))
}

export function Drivers() {
  const navigate = useNavigate()
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [routes, setRoutes] = useState<DriverRoute[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [vehicleFilter, setVehicleFilter] = useState('ALL')
  const [performanceFilter, setPerformanceFilter] = useState<PerformanceFilter>('ALL')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [savingAvailability, setSavingAvailability] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const response = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
      const data = response.data
      setMonitoring(data)
      setSelectedId(current => current && data.drivers.some(driver => driver.id === current)
        ? current
        : data.drivers[0]?.id ?? null)

      const routeResults = await Promise.all(data.drivers.map(async driver => {
        try {
          const route = await api.get<DriverRoute>(`/api/dispatch/drivers/${driver.id}/route`)
          return route.data
        } catch {
          return null
        }
      }))
      setRoutes(routeResults.filter((route): route is DriverRoute => route !== null))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load().catch(() => setMessage('Não foi possível carregar a gestão de motoristas.'))
    const timer = window.setInterval(() => load().catch(() => undefined), 15000)
    return () => window.clearInterval(timer)
  }, [load])

  const selectedDriver = monitoring?.drivers.find(driver => driver.id === selectedId) ?? null
  const selectedOrders = useMemo(
    () => monitoring?.orders.filter(order => order.driverId === selectedId) ?? [],
    [monitoring, selectedId]
  )
  const selectedActiveOrders = selectedOrders.filter(order => ACTIVE_STATUSES.has(order.status))
  const selectedRoute = routes.find(route => route.driverId === selectedId) ?? null
  const mapPoints = useMemo(() => createMapPoints(selectedDriver, selectedRoute), [selectedDriver, selectedRoute])

  const filteredDrivers = useMemo(() => {
    if (!monitoring) return []
    const term = search.trim().toLowerCase()

    return monitoring.drivers.filter(driver => {
      const status = operationalStatus(driver, monitoring.orders)
      const score = operationalScore(driver, monitoring.orders)
      const matchesSearch = !term
        || driver.name.toLowerCase().includes(term)
        || driver.vehiclePlate.toLowerCase().includes(term)
        || driver.vehicleModel.toLowerCase().includes(term)
      const matchesStatus = statusFilter === 'ALL' || status === statusFilter
      const matchesVehicle = vehicleFilter === 'ALL' || driver.vehicleModel === vehicleFilter
      return matchesSearch && matchesStatus && matchesVehicle && performanceMatches(score, performanceFilter)
    })
  }, [monitoring, performanceFilter, search, statusFilter, vehicleFilter])

  async function handleCreated(driverId: number) {
    setCreateOpen(false)
    await load()
    setSelectedId(driverId)
    setMessage('Motorista cadastrado e disponível para a operação conforme configuração informada.')
  }

  async function toggleAvailability() {
    if (!selectedDriver) return
    setSavingAvailability(true)
    try {
      const nextAvailability = !selectedDriver.available
      await api.patch(`/api/drivers/${selectedDriver.id}/availability`, { available: nextAvailability })
      await load()
      setSelectedId(selectedDriver.id)
      setMessage(nextAvailability
        ? `${selectedDriver.name} está disponível para novos despachos.`
        : `${selectedDriver.name} foi marcado como indisponível para novos despachos.`)
    } catch {
      setMessage('Não foi possível alterar a disponibilidade do motorista.')
    } finally {
      setSavingAvailability(false)
    }
  }

  function clearFilters() {
    setSearch('')
    setStatusFilter('ALL')
    setVehicleFilter('ALL')
    setPerformanceFilter('ALL')
  }

  if (!monitoring) {
    return <article className="panel empty"><RefreshCcw className="spin" /><h2>Carregando motoristas...</h2></article>
  }

  const statuses = monitoring.drivers.map(driver => operationalStatus(driver, monitoring.orders))
  const averageScore = monitoring.drivers.length
    ? Math.round(monitoring.drivers.reduce((sum, driver) => sum + operationalScore(driver, monitoring.orders), 0) / monitoring.drivers.length)
    : 0
  const freeCapacity = monitoring.drivers.reduce(
    (sum, driver) => sum + Math.max(0, driver.maxCapacity - driver.currentLoad),
    0
  )
  const activeRoutes = routes.filter(route => route.totalStops > 0).length
  const attentionCount = statuses.filter(status => status === 'ATTENTION').length

  const kpis = [
    ['Motoristas', monitoring.drivers.length, UsersRound, 'blue'],
    ['Disponíveis', statuses.filter(status => status === 'AVAILABLE').length, CheckCircle2, 'green'],
    ['Em rota', statuses.filter(status => status === 'ON_ROUTE').length, Truck, 'blue'],
    ['Atenção', attentionCount, AlertTriangle, 'amber'],
    ['Indisponíveis', statuses.filter(status => status === 'UNAVAILABLE').length, Power, 'slate'],
    ['Capacidade livre', freeCapacity, Gauge, 'cyan'],
    ['Score operacional', `${averageScore}/100`, Star, 'violet']
  ] as const

  const selectedStatus = selectedDriver ? operationalStatus(selectedDriver, monitoring.orders) : 'UNAVAILABLE'
  const selectedScore = selectedDriver ? operationalScore(selectedDriver, monitoring.orders) : 0
  const nextStop = selectedRoute?.stops[0] ?? null
  const nextOrder = nextStop
    ? monitoring.orders.find(order => order.id === nextStop.orderId) ?? null
    : selectedActiveOrders[0] ?? null
  const vehicleModels = [...new Set(monitoring.drivers.map(driver => driver.vehicleModel))].sort()
  const selectedLoadPercent = selectedDriver?.maxCapacity
    ? Math.round((selectedDriver.currentLoad / selectedDriver.maxCapacity) * 100)
    : 0
  const selectedAverageRisk = selectedActiveOrders.length
    ? Math.round(selectedActiveOrders.reduce((sum, order) => sum + order.riskPercent, 0) / selectedActiveOrders.length)
    : 0
  const deliveredCount = selectedOrders.filter(order => order.status === 'DELIVERED').length
  const failedCount = selectedOrders.filter(order => order.status === 'DELIVERY_FAILED').length
  const mapPolyline = mapPoints.map(point => `${point.x},${point.y}`).join(' ')

  return (
    <div className="driversPage driversCommandCenter">
      <section className="driversHeader">
        <div>
          <div className="driversEyebrow">ÚLTIMA MILHA · CENTRO OPERACIONAL</div>
          <h1>Motoristas</h1>
          <p>Gerencie disponibilidade, capacidade, entregas, posição persistida e rotas com atualização automática a cada 15 segundos.</p>
        </div>
        <div className="driversHeaderActions">
          <button type="button" disabled={loading} onClick={() => load()}><RefreshCcw className={loading ? 'spin' : ''} size={16} /> Atualizar</button>
          <button className="primary" type="button" onClick={() => setCreateOpen(true)}><UserPlus size={16} /> Novo motorista</button>
        </div>
      </section>

      {message && <div className="driversNotice">{message}</div>}

      <section className="driverMetrics">
        {kpis.map(([label, value, Icon, tone]) => (
          <article key={label} className={`driverMetricCard ${tone}`}>
            <span className="driverMetricIcon"><Icon size={19} /></span>
            <div><small>{label}</small><strong>{value}</strong></div>
          </article>
        ))}
      </section>

      <section className="driversToolbar">
        <label className="driversSearch"><Search size={16} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar motorista, placa ou veículo..." /></label>
        <select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}>
          <option value="ALL">Status</option>
          <option value="AVAILABLE">Disponível</option>
          <option value="ON_ROUTE">Em rota</option>
          <option value="ATTENTION">Atenção</option>
          <option value="UNAVAILABLE">Indisponível</option>
        </select>
        <select value={vehicleFilter} onChange={event => setVehicleFilter(event.target.value)}>
          <option value="ALL">Veículo</option>
          {vehicleModels.map(model => <option key={model} value={model}>{model}</option>)}
        </select>
        <select value={performanceFilter} onChange={event => setPerformanceFilter(event.target.value as PerformanceFilter)}>
          <option value="ALL">Desempenho</option>
          <option value="HIGH">Score 90+</option>
          <option value="MEDIUM">Score 75–89</option>
          <option value="ATTENTION">Score abaixo de 75</option>
        </select>
        <button className="driversClearFilters" type="button" onClick={clearFilters}>Limpar filtros</button>
        <span className="driversCount">{filteredDrivers.length} motorista(s) · {activeRoutes} rota(s) ativa(s)</span>
      </section>

      <section className="driversWorkspace">
        <article className="driversTablePanel">
          <div className="driversTableHeading">
            <div><strong>Equipe operacional</strong><span>Dados reais do Smart Dispatch e da API de Motoristas</span></div>
            <span>{monitoring.drivers.length} cadastrados</span>
          </div>
          <div className="tableWrap">
            <table className="driversTable">
              <thead>
                <tr><th>Motorista</th><th>Status</th><th>Veículo</th><th>Rota atual</th><th>Carga</th><th>Próxima parada</th><th>Score</th><th>Ação</th></tr>
              </thead>
              <tbody>
                {filteredDrivers.map(driver => {
                  const status = operationalStatus(driver, monitoring.orders)
                  const score = operationalScore(driver, monitoring.orders)
                  const route = routes.find(item => item.driverId === driver.id)
                  const next = route?.stops[0]
                  const loadPercent = driver.maxCapacity > 0 ? Math.round(driver.currentLoad * 100 / driver.maxCapacity) : 100

                  return (
                    <tr key={driver.id} className={selectedId === driver.id ? 'selected' : ''} onClick={() => setSelectedId(driver.id)}>
                      <td>
                        <div className="driverCellIdentity">
                          <span className="driverAvatarSmall">{driver.name.charAt(0).toUpperCase()}</span>
                          <div><strong>{driver.name}</strong><small>ID #{String(driver.id).padStart(3, '0')}</small></div>
                        </div>
                      </td>
                      <td><span className={`driverStatus ${status.toLowerCase()}`}><i />{statusLabel(status)}</span></td>
                      <td><strong>{driver.vehicleModel}</strong><small>{driver.vehiclePlate}</small></td>
                      <td>{route?.totalStops ? <><strong>RT-{String(driver.id).padStart(3, '0')}</strong><small>{route.totalDistanceKm.toFixed(2)} km · {route.totalStops} parada(s)</small></> : <span className="driverMuted">Sem rota</span>}</td>
                      <td><div className="driverLoadCell"><strong>{driver.currentLoad}/{driver.maxCapacity}</strong><span><i style={{ width: `${Math.min(100, loadPercent)}%` }} /></span><small>{loadPercent}% ocupado</small></div></td>
                      <td>{next ? <><strong>#{next.orderNumber}</strong><small>ETA +{next.etaFromNowMinutes} min</small></> : <span className="driverMuted">Aguardando</span>}</td>
                      <td><span className={`driverScore ${score < 75 ? 'attention' : score >= 90 ? 'high' : ''}`}>{score}</span></td>
                      <td><button className="rowAction" type="button" onClick={event => { event.stopPropagation(); setSelectedId(driver.id) }} aria-label="Ver motorista"><Eye size={15} /></button></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
          {!filteredDrivers.length && <div className="driverEmptyTable">Nenhum motorista corresponde aos filtros atuais.</div>}
          <div className="driversTableFooter"><span>Mostrando {filteredDrivers.length} de {monitoring.drivers.length} motoristas</span><span>Atualização operacional: 15s</span></div>
        </article>

        <aside className="driverInspector">
          {selectedDriver ? (
            <>
              <div className="driverProfileTop">
                <div className="driverAvatarLarge">{selectedDriver.name.charAt(0).toUpperCase()}</div>
                <div className="driverProfileIdentity">
                  <small>MOTORISTA · ID #{String(selectedDriver.id).padStart(3, '0')}</small>
                  <h2>{selectedDriver.name}</h2>
                  <span className={`driverStatus ${selectedStatus.toLowerCase()}`}><i />{statusLabel(selectedStatus)}</span>
                </div>
                <div className="scoreBox"><strong>{selectedScore}</strong><small>Score operacional</small></div>
              </div>

              <div className="driverQuickStats">
                <span><Gauge size={15}/><small>Carga</small><b>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</b></span>
                <span><Navigation size={15}/><small>Paradas</small><b>{selectedRoute?.totalStops ?? 0}</b></span>
                <span><MapPin size={15}/><small>Distância</small><b>{selectedRoute ? `${selectedRoute.totalDistanceKm.toFixed(2)} km` : '0 km'}</b></span>
                <span><Clock3 size={15}/><small>Tempo rota</small><b>{selectedRoute ? `${selectedRoute.estimatedRouteMinutes} min` : '0 min'}</b></span>
              </div>

              <section className="driverSection driverInfoGridSection">
                <div className="driverSectionTitle"><h3>Informações operacionais</h3><Activity size={16}/></div>
                <div className="driverInfoGrid">
                  <div><small>Veículo</small><strong>{selectedDriver.vehicleModel}</strong><span>{selectedDriver.vehiclePlate}</span></div>
                  <div><small>Capacidade</small><strong>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</strong><span>{selectedLoadPercent}% ocupado</span></div>
                  <div><small>Latitude</small><strong>{formatCoordinate(selectedDriver.latitude)}</strong><span>posição persistida</span></div>
                  <div><small>Longitude</small><strong>{formatCoordinate(selectedDriver.longitude)}</strong><span>posição persistida</span></div>
                </div>
              </section>

              <section className="driverSection">
                <div className="driverSectionTitle"><h3>Rota operacional</h3><span className="driverDataBadge">DADOS DA API</span></div>
                <div className="driverOperationalMap">
                  <div className="driverMapGrid" />
                  {mapPoints.length > 1 && <svg viewBox="0 0 100 54" preserveAspectRatio="none" aria-label="Traçado esquemático da rota">
                    <polyline points={mapPolyline} fill="none" stroke="currentColor" strokeWidth="1.3" vectorEffect="non-scaling-stroke" />
                  </svg>}
                  {mapPoints.map(point => (
                    <div
                      key={point.id}
                      className={`driverMapPoint ${point.kind}`}
                      style={{ left: `${point.x}%`, top: `${point.y / 54 * 100}%` }}
                      title={point.label}
                    >
                      {point.kind === 'driver' ? <Truck size={14}/> : point.position}
                    </div>
                  ))}
                  <div className="driverMapLegend"><span>Mapa operacional esquemático</span><small>Sem mapa viário, trânsito ou GPS contínuo</small></div>
                </div>
                {selectedRoute?.totalStops ? (
                  <div className="driverRouteSummary">
                    <div><Navigation size={18}/><span><strong>RT-{String(selectedDriver.id).padStart(3, '0')}</strong><small>{selectedRoute.totalStops} parada(s) · {selectedRoute.totalDistanceKm.toFixed(2)} km · {selectedRoute.estimatedRouteMinutes} min</small></span></div>
                    <button type="button" onClick={() => navigate('/routes')}>Abrir rota</button>
                  </div>
                ) : <div className="driverEmpty">Nenhuma rota ativa para este motorista.</div>}
              </section>

              <section className="driverInspectorSplit">
                <div className="driverSection nextDeliverySection">
                  <div className="driverSectionTitle"><h3>Próxima entrega</h3><PackageCheck size={16}/></div>
                  {nextOrder ? (
                    <div className="nextDelivery">
                      <span className="nextDeliveryIndex">{nextStop?.position ?? 1}</span>
                      <div><strong>{nextOrder.customerName}</strong><span>#{nextOrder.orderNumber}</span><small>{nextOrder.destinationLabel}</small><b>ETA +{nextStop?.etaFromNowMinutes ?? nextOrder.etaMinutes ?? '—'} min · risco {nextOrder.riskPercent}%</b></div>
                    </div>
                  ) : <div className="driverEmpty">Nenhuma próxima entrega calculada.</div>}
                </div>

                <div className="driverSection driverPerformanceSection">
                  <div className="driverSectionTitle"><h3>Operação atual</h3><Activity size={16}/></div>
                  <div className="driverPerformanceBars">
                    <label><span>Score</span><b>{selectedScore}%</b><i><em style={{ width: `${selectedScore}%` }} /></i></label>
                    <label><span>Ocupação</span><b>{selectedLoadPercent}%</b><i><em style={{ width: `${Math.min(100, selectedLoadPercent)}%` }} /></i></label>
                    <label><span>Risco médio</span><b>{selectedAverageRisk}%</b><i><em className="risk" style={{ width: `${Math.min(100, selectedAverageRisk)}%` }} /></i></label>
                  </div>
                  <div className="driverOperationFacts"><span>{selectedActiveOrders.length} ativas</span><span>{deliveredCount} entregues</span><span>{failedCount} falhas</span></div>
                </div>
              </section>

              <section className="driverSection driverTimelineSection">
                <div className="driverSectionTitle"><h3>Sequência da rota</h3><Navigation size={16}/></div>
                <div className="driverRouteTimeline">
                  {selectedRoute?.stops.length ? selectedRoute.stops.slice(0, 5).map(stop => {
                    const order = monitoring.orders.find(item => item.id === stop.orderId)
                    return (
                      <button type="button" key={stop.orderId} onClick={() => navigate('/deliveries')}>
                        <span className="routeStopPosition">{stop.position}</span>
                        <span><strong>#{stop.orderNumber}</strong><small>{order?.customerName ?? 'Cliente'} · {stop.distanceFromPreviousKm.toFixed(2)} km</small></span>
                        <b>+{stop.etaFromNowMinutes} min</b>
                      </button>
                    )
                  }) : <div className="driverEmpty">Sem paradas planejadas.</div>}
                </div>
              </section>

              <div className="driverActions">
                <button type="button" onClick={() => navigate('/routes')}><Navigation size={16}/> Ver rota</button>
                <button type="button" onClick={() => navigate('/deliveries')}><Truck size={16}/> Ver entregas</button>
                <button className={selectedDriver.available ? 'dangerSoft' : 'successSoft'} type="button" disabled={savingAvailability} onClick={toggleAvailability}><Power size={16}/>{savingAvailability ? 'Salvando...' : selectedDriver.available ? 'Indisponibilizar' : 'Disponibilizar'}</button>
              </div>

              <p className="driverScoreNote">Score, ocupação, risco, rota e ETA são derivados dos dados atuais da operação. GPS contínuo, telemetria, trânsito e mapa viário permanecem como integrações externas pendentes.</p>
            </>
          ) : <div className="driverInspectorEmpty"><UsersRound size={28}/><strong>Selecione um motorista</strong><span>Os dados operacionais aparecerão aqui.</span></div>}
        </aside>
      </section>

      {createOpen && <DriverCreateModal onClose={() => setCreateOpen(false)} onSaved={handleCreated} />}
    </div>
  )
}
