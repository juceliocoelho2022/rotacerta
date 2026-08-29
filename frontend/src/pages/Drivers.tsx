import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  CarFront,
  CheckCircle2,
  Eye,
  Gauge,
  MapPin,
  Navigation,
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

export function Drivers() {
  const navigate = useNavigate()
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [routes, setRoutes] = useState<DriverRoute[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [vehicleFilter, setVehicleFilter] = useState('ALL')
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

  const filteredDrivers = useMemo(() => {
    if (!monitoring) return []
    const term = search.trim().toLowerCase()

    return monitoring.drivers.filter(driver => {
      const status = operationalStatus(driver, monitoring.orders)
      const matchesSearch = !term
        || driver.name.toLowerCase().includes(term)
        || driver.vehiclePlate.toLowerCase().includes(term)
        || driver.vehicleModel.toLowerCase().includes(term)
      const matchesStatus = statusFilter === 'ALL' || status === statusFilter
      const matchesVehicle = vehicleFilter === 'ALL' || driver.vehicleModel === vehicleFilter
      return matchesSearch && matchesStatus && matchesVehicle
    })
  }, [monitoring, search, statusFilter, vehicleFilter])

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

  const kpis = [
    ['Motoristas', monitoring.drivers.length, UsersRound],
    ['Disponíveis', statuses.filter(status => status === 'AVAILABLE').length, CheckCircle2],
    ['Em rota', statuses.filter(status => status === 'ON_ROUTE').length, Truck],
    ['Atenção', statuses.filter(status => status === 'ATTENTION').length, AlertTriangle],
    ['Indisponíveis', statuses.filter(status => status === 'UNAVAILABLE').length, Power],
    ['Capacidade livre', freeCapacity, Gauge],
    ['Score operacional', `${averageScore}/100`, Star]
  ] as const

  const selectedStatus = selectedDriver ? operationalStatus(selectedDriver, monitoring.orders) : 'UNAVAILABLE'
  const selectedScore = selectedDriver ? operationalScore(selectedDriver, monitoring.orders) : 0
  const nextStop = selectedRoute?.stops[0] ?? null
  const nextOrder = nextStop
    ? monitoring.orders.find(order => order.id === nextStop.orderId) ?? null
    : selectedActiveOrders[0] ?? null
  const vehicleModels = [...new Set(monitoring.drivers.map(driver => driver.vehicleModel))].sort()

  return (
    <div className="driversPage">
      <section className="driversHeader">
        <div>
          <h1>Motoristas</h1>
          <p>Disponibilidade, carga, posição persistida, entregas atribuídas e rota da equipe de última milha.</p>
        </div>
        <div className="driversHeaderActions">
          <button type="button" disabled={loading} onClick={() => load()}><RefreshCcw className={loading ? 'spin' : ''} size={16} /> Atualizar</button>
          <button className="primary" type="button" onClick={() => setCreateOpen(true)}><UserPlus size={16} /> Novo motorista</button>
        </div>
      </section>

      {message && <div className="driversNotice">{message}</div>}

      <section className="driverMetrics">
        {kpis.map(([label, value, Icon]) => (
          <article key={label}><span><Icon size={19} /></span><div><small>{label}</small><strong>{value}</strong></div></article>
        ))}
      </section>

      <section className="driversToolbar">
        <label className="driversSearch"><Search size={16} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar motorista, placa ou veículo..." /></label>
        <select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}>
          <option value="ALL">Todos os status</option>
          <option value="AVAILABLE">Disponível</option>
          <option value="ON_ROUTE">Em rota</option>
          <option value="ATTENTION">Atenção</option>
          <option value="UNAVAILABLE">Indisponível</option>
        </select>
        <select value={vehicleFilter} onChange={event => setVehicleFilter(event.target.value)}>
          <option value="ALL">Todos os veículos</option>
          {vehicleModels.map(model => <option key={model} value={model}>{model}</option>)}
        </select>
        <span className="driversCount">{filteredDrivers.length} motorista(s) • {activeRoutes} rota(s) ativa(s)</span>
      </section>

      <section className="driversWorkspace">
        <article className="driversTablePanel">
          <div className="tableWrap">
            <table className="driversTable">
              <thead>
                <tr><th>Motorista</th><th>Status</th><th>Veículo</th><th>Carga</th><th>Rota</th><th>Próxima parada</th><th>Score</th><th>Ação</th></tr>
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
                      <td><strong>{driver.name}</strong><small>{formatCoordinate(driver.latitude)}, {formatCoordinate(driver.longitude)}</small></td>
                      <td><span className={`driverStatus ${status.toLowerCase()}`}><i />{statusLabel(status)}</span></td>
                      <td><strong>{driver.vehiclePlate}</strong><small>{driver.vehicleModel}</small></td>
                      <td><div className="driverLoadCell"><strong>{driver.currentLoad}/{driver.maxCapacity}</strong><span><i style={{ width: `${Math.min(100, loadPercent)}%` }} /></span></div></td>
                      <td>{route?.totalStops ? <><strong>RT-{String(driver.id).padStart(3, '0')}</strong><small>{route.totalDistanceKm.toFixed(2)} km • {route.totalStops} parada(s)</small></> : <span className="driverMuted">Sem rota</span>}</td>
                      <td>{next ? <><strong>#{next.orderNumber}</strong><small>ETA +{next.etaFromNowMinutes} min</small></> : <span className="driverMuted">Aguardando</span>}</td>
                      <td><span className={`driverScore ${score < 70 ? 'attention' : score >= 90 ? 'high' : ''}`}>{score}</span></td>
                      <td><button className="rowAction" type="button" onClick={event => { event.stopPropagation(); setSelectedId(driver.id) }} aria-label="Ver motorista"><Eye size={15} /></button></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
          {!filteredDrivers.length && <div className="driverEmptyTable">Nenhum motorista corresponde aos filtros atuais.</div>}
        </article>

        <aside className="driverInspector">
          {selectedDriver ? (
            <>
              <div className="driverProfileTop">
                <div className="driverAvatarLarge">{selectedDriver.name.charAt(0).toUpperCase()}</div>
                <div className="driverProfileIdentity">
                  <small>MOTORISTA</small>
                  <h2>{selectedDriver.name}</h2>
                  <span className={`driverStatus ${selectedStatus.toLowerCase()}`}><i />{statusLabel(selectedStatus)}</span>
                </div>
                <div className="scoreBox"><strong>{selectedScore}</strong><small>Score operacional</small></div>
              </div>

              <div className="driverQuickStats">
                <span><small>Carga</small><b>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</b></span>
                <span><small>Paradas</small><b>{selectedRoute?.totalStops ?? 0}</b></span>
                <span><small>Distância</small><b>{selectedRoute ? `${selectedRoute.totalDistanceKm.toFixed(2)} km` : '0 km'}</b></span>
                <span><small>Tempo rota</small><b>{selectedRoute ? `${selectedRoute.estimatedRouteMinutes} min` : '0 min'}</b></span>
              </div>

              <section className="driverSection">
                <h3>Veículo atual</h3>
                <div className="driverVehicle"><CarFront size={18}/><div><strong>{selectedDriver.vehicleModel}</strong><span>Placa {selectedDriver.vehiclePlate}</span></div></div>
              </section>

              <section className="driverSection">
                <h3>Posição operacional persistida</h3>
                <div className="driverCoordinates">
                  <MapPin size={18}/>
                  <div><strong>{formatCoordinate(selectedDriver.latitude)}, {formatCoordinate(selectedDriver.longitude)}</strong><span>Atualizada pela API de localização do motorista.</span></div>
                </div>
                <p className="driverIntegrationNote">GPS/telemetria contínua em tempo real: integração externa pendente.</p>
              </section>

              <section className="driverSection">
                <h3>Rota atual</h3>
                {selectedRoute?.totalStops ? (
                  <div className="driverRouteSummary">
                    <div><Navigation size={18}/><span><strong>RT-{String(selectedDriver.id).padStart(3, '0')}</strong><small>{selectedRoute.totalStops} parada(s) • {selectedRoute.totalDistanceKm.toFixed(2)} km • {selectedRoute.estimatedRouteMinutes} min</small></span></div>
                    <button type="button" onClick={() => navigate('/routes')}>Abrir rotas</button>
                  </div>
                ) : <div className="driverEmpty">Nenhuma rota ativa para este motorista.</div>}
              </section>

              <section className="driverSection">
                <h3>Próxima entrega</h3>
                {nextOrder ? (
                  <div className="nextDelivery">
                    <MapPin size={18}/>
                    <div><strong>{nextOrder.customerName}</strong><span>#{nextOrder.orderNumber} • {nextOrder.destinationLabel}</span><small>ETA +{nextStop?.etaFromNowMinutes ?? nextOrder.etaMinutes ?? '—'} min • Risco {nextOrder.riskPercent}%</small></div>
                  </div>
                ) : <div className="driverEmpty">Nenhuma próxima entrega calculada.</div>}
              </section>

              <section className="driverSection">
                <h3>Entregas atribuídas</h3>
                <div className="driverAssignments">
                  {selectedActiveOrders.length ? selectedActiveOrders.slice(0, 5).map(order => (
                    <button type="button" key={order.id} onClick={() => navigate('/deliveries')}>
                      <span><strong>#{order.orderNumber}</strong><small>{order.customerName}</small></span>
                      <span><b>{order.status.replaceAll('_', ' ')}</b><small>Risco {order.riskPercent}%</small></span>
                    </button>
                  )) : <div className="driverEmpty">Sem entregas ativas atribuídas.</div>}
                </div>
              </section>

              <div className="driverActions">
                <button className={selectedDriver.available ? 'dangerSoft' : 'successSoft'} type="button" disabled={savingAvailability} onClick={toggleAvailability}><Power size={16}/>{savingAvailability ? 'Salvando...' : selectedDriver.available ? 'Marcar indisponível' : 'Marcar disponível'}</button>
                <button type="button" onClick={() => navigate('/routes')}><Navigation size={16}/> Ver rota</button>
                <button type="button" onClick={() => navigate('/deliveries')}><Truck size={16}/> Ver entregas</button>
              </div>

              <p className="driverScoreNote">O score operacional é um indicador derivado do risco das entregas ativas e da ocupação da capacidade. Não representa avaliação trabalhista nem usa telemetria externa.</p>
            </>
          ) : <div className="driverInspectorEmpty"><UsersRound size={28}/><strong>Selecione um motorista</strong><span>Os dados operacionais aparecerão aqui.</span></div>}
        </aside>
      </section>

      {createOpen && <DriverCreateModal onClose={() => setCreateOpen(false)} onSaved={handleCreated} />}
    </div>
  )
}
