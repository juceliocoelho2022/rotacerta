import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  CarFront,
  CheckCircle2,
  Eye,
  Gauge,
  MapPin,
  MoreHorizontal,
  Navigation,
  RefreshCcw,
  Search,
  Star,
  Truck,
  UserPlus,
  UsersRound
} from 'lucide-react'
import { api, type DriverRoute, type MonitoringDriver, type MonitoringOrder, type OperationsMonitoring } from '../services/api'

const ACTIVE_STATUSES = new Set(['PICKING', 'PACKING', 'READY_FOR_SHIPMENT', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'])

type DriverOperationalStatus = 'AVAILABLE' | 'ON_ROUTE' | 'PAUSED' | 'INCIDENT' | 'OFFLINE'

function operationalStatus(driver: MonitoringDriver, orders: MonitoringOrder[]): DriverOperationalStatus {
  const assigned = orders.filter(order => order.driverId === driver.id)
  const hasIncident = assigned.some(order => order.status === 'DELIVERY_FAILED' || order.riskPercent >= 90)
  const hasActiveRoute = assigned.some(order => ACTIVE_STATUSES.has(order.status))

  if (hasIncident) return 'INCIDENT'
  if (!driver.available && hasActiveRoute) return 'PAUSED'
  if (!driver.available) return 'OFFLINE'
  if (hasActiveRoute) return 'ON_ROUTE'
  return 'AVAILABLE'
}

function statusLabel(status: DriverOperationalStatus) {
  return {
    AVAILABLE: 'Disponível',
    ON_ROUTE: 'Em rota',
    PAUSED: 'Em pausa',
    INCIDENT: 'Ocorrência',
    OFFLINE: 'Offline'
  }[status]
}

function scoreFor(driver: MonitoringDriver, orders: MonitoringOrder[]) {
  const assigned = orders.filter(order => order.driverId === driver.id)
  const riskAvg = assigned.length ? assigned.reduce((sum, order) => sum + order.riskPercent, 0) / assigned.length : 0
  const failed = assigned.filter(order => order.status === 'DELIVERY_FAILED').length
  const delivered = assigned.filter(order => order.status === 'DELIVERED').length
  const completed = delivered + failed
  const success = completed ? delivered / completed : 1
  const loadPenalty = driver.maxCapacity ? (driver.currentLoad / driver.maxCapacity) * 8 : 0
  return Math.max(0, Math.min(100, Math.round(100 - riskAvg * 0.28 - failed * 6 - loadPenalty + success * 4)))
}

function efficiencyFor(driver: MonitoringDriver, orders: MonitoringOrder[]) {
  const assigned = orders.filter(order => order.driverId === driver.id)
  if (!assigned.length) return 100
  const avgRisk = assigned.reduce((sum, order) => sum + order.riskPercent, 0) / assigned.length
  return Math.max(0, Math.min(100, Math.round(100 - avgRisk * 0.35)))
}

export function Drivers() {
  const navigate = useNavigate()
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [routes, setRoutes] = useState<DriverRoute[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [vehicleFilter, setVehicleFilter] = useState('ALL')
  const [performanceFilter, setPerformanceFilter] = useState('ALL')
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    const response = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
    const data = response.data
    setMonitoring(data)
    setSelectedId(current => current ?? data.drivers[0]?.id ?? null)

    const routeResults = await Promise.all(data.drivers.map(async driver => {
      try {
        const route = await api.get<DriverRoute>(`/api/dispatch/drivers/${driver.id}/route`)
        return route.data
      } catch {
        return null
      }
    }))
    setRoutes(routeResults.filter((route): route is DriverRoute => route !== null))
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
  const selectedRoute = routes.find(route => route.driverId === selectedId) ?? null

  const filteredDrivers = useMemo(() => {
    if (!monitoring) return []
    const term = search.trim().toLowerCase()
    return monitoring.drivers.filter(driver => {
      const status = operationalStatus(driver, monitoring.orders)
      const score = scoreFor(driver, monitoring.orders)
      const matchesSearch = !term || driver.name.toLowerCase().includes(term) || driver.vehiclePlate.toLowerCase().includes(term) || driver.vehicleModel.toLowerCase().includes(term)
      const matchesStatus = statusFilter === 'ALL' || status === statusFilter
      const matchesVehicle = vehicleFilter === 'ALL' || driver.vehicleModel === vehicleFilter
      const matchesPerformance = performanceFilter === 'ALL' || (performanceFilter === 'HIGH' && score >= 90) || (performanceFilter === 'MEDIUM' && score >= 75 && score < 90) || (performanceFilter === 'ATTENTION' && score < 75)
      return matchesSearch && matchesStatus && matchesVehicle && matchesPerformance
    })
  }, [monitoring, performanceFilter, search, statusFilter, vehicleFilter])

  if (!monitoring) {
    return <article className="panel empty"><RefreshCcw className="spin" /><h2>Carregando motoristas...</h2></article>
  }

  const statuses = monitoring.drivers.map(driver => operationalStatus(driver, monitoring.orders))
  const averageScore = monitoring.drivers.length
    ? Math.round(monitoring.drivers.reduce((sum, driver) => sum + scoreFor(driver, monitoring.orders), 0) / monitoring.drivers.length)
    : 0

  const kpis = [
    ['Motoristas cadastrados', monitoring.drivers.length, UsersRound],
    ['Disponíveis', statuses.filter(status => status === 'AVAILABLE').length, CheckCircle2],
    ['Em rota', statuses.filter(status => status === 'ON_ROUTE').length, Truck],
    ['Em pausa', statuses.filter(status => status === 'PAUSED').length, Gauge],
    ['Com ocorrência', statuses.filter(status => status === 'INCIDENT').length, AlertTriangle],
    ['Offline', statuses.filter(status => status === 'OFFLINE').length, UsersRound],
    ['Driver Score médio', `${averageScore}/100`, Star]
  ] as const

  const delivered = selectedOrders.filter(order => order.status === 'DELIVERED').length
  const failed = selectedOrders.filter(order => order.status === 'DELIVERY_FAILED').length
  const active = selectedOrders.filter(order => ACTIVE_STATUSES.has(order.status)).length
  const nextOrder = selectedRoute?.stops[0]
    ? monitoring.orders.find(order => order.id === selectedRoute.stops[0].orderId) ?? null
    : selectedOrders.find(order => ACTIVE_STATUSES.has(order.status)) ?? null
  const nextStop = selectedRoute?.stops[0] ?? null
  const driverScore = selectedDriver ? scoreFor(selectedDriver, monitoring.orders) : 0
  const efficiency = selectedDriver ? efficiencyFor(selectedDriver, monitoring.orders) : 0
  const selectedStatus = selectedDriver ? operationalStatus(selectedDriver, monitoring.orders) : 'OFFLINE'

  const vehicleModels = [...new Set(monitoring.drivers.map(driver => driver.vehicleModel))].sort()

  return (
    <div className="driversPage">
      <section className="driversHeader">
        <div>
          <h1>Motoristas</h1>
          <p>Gestão operacional, disponibilidade, desempenho e carga da equipe de última milha.</p>
        </div>
        <div className="driversHeaderActions">
          <button onClick={() => load()}><RefreshCcw size={16} /> Atualizar</button>
          <button className="primary" onClick={() => setMessage('Cadastro de motorista preparado para a próxima sprint.')}><UserPlus size={16} /> Novo motorista</button>
        </div>
      </section>

      {message && <div className="driversNotice">{message}</div>}

      <section className="driverMetrics">
        {kpis.map(([label, value, Icon]) => (
          <article key={label}><Icon size={20} /><div><span>{label}</span><strong>{value}</strong></div></article>
        ))}
      </section>

      <section className="driversToolbar">
        <label className="driversSearch"><Search size={16} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar motorista ou veículo..." /></label>
        <select value={statusFilter} onChange={event => setStatusFilter(event.target.value)}>
          <option value="ALL">Status</option>
          <option value="AVAILABLE">Disponível</option>
          <option value="ON_ROUTE">Em rota</option>
          <option value="PAUSED">Em pausa</option>
          <option value="INCIDENT">Ocorrência</option>
          <option value="OFFLINE">Offline</option>
        </select>
        <select value={vehicleFilter} onChange={event => setVehicleFilter(event.target.value)}>
          <option value="ALL">Veículo</option>
          {vehicleModels.map(model => <option key={model} value={model}>{model}</option>)}
        </select>
        <select value={performanceFilter} onChange={event => setPerformanceFilter(event.target.value)}>
          <option value="ALL">Desempenho</option>
          <option value="HIGH">Score 90+</option>
          <option value="MEDIUM">Score 75–89</option>
          <option value="ATTENTION">Abaixo de 75</option>
        </select>
        <span className="driversCount">{filteredDrivers.length} motorista(s)</span>
      </section>

      <section className="driversWorkspace">
        <article className="driversTablePanel">
          <div className="tableWrap">
            <table className="driversTable">
              <thead><tr><th>Motorista</th><th>Status</th><th>Veículo</th><th>Rota</th><th>Entregas</th><th>Eficiência</th><th>Score</th><th>Ações</th></tr></thead>
              <tbody>
                {filteredDrivers.map(driver => {
                  const orders = monitoring.orders.filter(order => order.driverId === driver.id)
                  const route = routes.find(item => item.driverId === driver.id)
                  const delivered = orders.filter(order => order.status === 'DELIVERED').length
                  const status = operationalStatus(driver, monitoring.orders)
                  const score = scoreFor(driver, monitoring.orders)
                  const efficiency = efficiencyFor(driver, monitoring.orders)
                  return (
                    <tr key={driver.id} className={selectedId === driver.id ? 'selected' : ''} onClick={() => setSelectedId(driver.id)}>
                      <td><strong>{driver.name}</strong><small>{driver.latitude.toFixed(4)}, {driver.longitude.toFixed(4)}</small></td>
                      <td><span className={`driverStatus ${status.toLowerCase()}`}><i />{statusLabel(status)}</span></td>
                      <td><strong>{driver.vehiclePlate}</strong><small>{driver.vehicleModel}</small></td>
                      <td>{route?.totalStops ? `RT-${String(driver.id).padStart(3, '0')}` : '—'}</td>
                      <td>{delivered}/{Math.max(orders.length, driver.currentLoad)}</td>
                      <td>{efficiency}%</td>
                      <td><span className={`driverScore ${score < 75 ? 'attention' : score >= 90 ? 'high' : ''}`}>{score}</span></td>
                      <td><button className="rowAction" onClick={event => { event.stopPropagation(); setSelectedId(driver.id) }}><Eye size={15} /></button><button className="rowAction"><MoreHorizontal size={15} /></button></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </article>

        {selectedDriver && (
          <aside className="driverInspector">
            <div className="driverProfileTop">
              <div className="driverAvatarLarge">{selectedDriver.name.charAt(0)}</div>
              <div><h2>{selectedDriver.name}</h2><span className={`driverStatus ${selectedStatus.toLowerCase()}`}><i />{statusLabel(selectedStatus)}</span></div>
              <div className="scoreBox"><strong>{driverScore}</strong><small>Driver Score</small></div>
            </div>

            <div className="driverQuickStats">
              <span><small>Eficiência</small><b>{efficiency}%</b></span>
              <span><small>Carga</small><b>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</b></span>
              <span><small>Entregas</small><b>{selectedOrders.length}</b></span>
              <span><small>Concluídas</small><b>{delivered}</b></span>
            </div>

            <div className="driverSection">
              <h3>Veículo e rota</h3>
              <div className="driverVehicle"><CarFront size={18}/><div><strong>{selectedDriver.vehicleModel}</strong><span>{selectedDriver.vehiclePlate}</span></div></div>
              <div className="driverVehicle"><Navigation size={18}/><div><strong>{selectedRoute?.totalStops ? `RT-${String(selectedDriver.id).padStart(3, '0')}` : 'Sem rota ativa'}</strong><span>{selectedRoute ? `${selectedRoute.totalDistanceKm.toFixed(2)} km • ${selectedRoute.estimatedRouteMinutes} min` : 'Aguardando planejamento'}</span></div></div>
            </div>

            <div className="driverSection">
              <h3>Operação de hoje</h3>
              <dl className="driverOpsGrid">
                <div><dt>Programadas</dt><dd>{selectedOrders.length}</dd></div>
                <div><dt>Concluídas</dt><dd>{delivered}</dd></div>
                <div><dt>Em andamento</dt><dd>{active}</dd></div>
                <div><dt>Falhas</dt><dd>{failed}</dd></div>
              </dl>
            </div>

            <div className="driverSection">
              <h3>Próxima entrega</h3>
              {nextOrder ? <div className="nextDelivery"><MapPin size={18}/><div><strong>{nextOrder.customerName}</strong><span>{nextOrder.destinationLabel}</span><small>ETA +{nextStop?.etaFromNowMinutes ?? nextOrder.etaMinutes ?? '—'} min • Risco {nextOrder.riskPercent}%</small></div></div> : <div className="driverEmpty">Nenhuma próxima entrega calculada.</div>}
            </div>

            <div className="driverMiniMap">
              <div className="miniMapGrid" />
              <div className="miniDriver" style={{ left: '44%', top: '48%' }}>🚚</div>
              <span className="miniMapLabel">GPS atual: {selectedDriver.latitude.toFixed(4)}, {selectedDriver.longitude.toFixed(4)}</span>
            </div>

            <div className="driverActions">
              <button onClick={() => navigate('/routes')}><MapPin size={16}/> Ver localização</button>
              <button onClick={() => navigate('/routes')}><Navigation size={16}/> Ver rota</button>
              <button onClick={() => navigate('/deliveries')}><Truck size={16}/> Ver entregas</button>
              <button onClick={() => setMessage(`Contato operacional de ${selectedDriver.name} ainda será integrado.`)}><UsersRound size={16}/> Contatar</button>
            </div>

            <p className="driverScoreNote">O Driver Score atual é um indicador operacional do dia, calculado a partir de risco, carga e resultados das entregas. Não é usado para punição automática.</p>
          </aside>
        )}
      </section>
    </div>
  )
}
