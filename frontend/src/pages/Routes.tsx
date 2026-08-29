import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Gauge,
  MapPin,
  Navigation,
  Package,
  RefreshCcw,
  Route as RouteIcon,
  Sparkles,
  TrafficCone,
  Truck,
  UsersRound,
  WandSparkles
} from 'lucide-react'
import {
  api,
  type DispatchAssignment,
  type DriverRoute,
  type MonitoringDriver,
  type MonitoringOrder,
  type OperationsMonitoring,
  type RouteOptimization
} from '../services/api'

const routeColors = ['#1689ff', '#31c86b', '#f2a93b', '#a56cff', '#ef5350', '#21c7c7']

function fmt(value: number, digits = 1) {
  return Number(value).toLocaleString('pt-BR', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

function routeCode(driverId: number) {
  return `RT-${String(driverId).padStart(3, '0')}`
}

function RouteMap({
  monitoring,
  routes,
  selectedDriverId,
  onSelectDriver
}: {
  monitoring: OperationsMonitoring
  routes: DriverRoute[]
  selectedDriverId: number | null
  onSelectDriver: (driverId: number) => void
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
    const latSpan = Math.max(.01, maxLat - minLat)
    const lonSpan = Math.max(.01, maxLon - minLon)

    return (lat: number, lon: number) => ({
      x: 70 + ((lon - minLon) / lonSpan) * 860,
      y: 465 - ((lat - minLat) / latSpan) * 400
    })
  }, [monitoring])

  return (
    <div className="routesMapCanvas">
      <svg viewBox="0 0 1000 540" role="img" aria-label="Mapa operacional das rotas">
        <defs>
          <linearGradient id="routesBg" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#0b1d2c" />
            <stop offset="100%" stopColor="#07131f" />
          </linearGradient>
          <filter id="routeGlow"><feGaussianBlur stdDeviation="3" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
        </defs>
        <rect width="1000" height="540" fill="url(#routesBg)" />

        {Array.from({ length: 14 }).map((_, index) => <line key={`street-v-${index}`} x1={35 + index * 78} y1="0" x2={15 + index * 72} y2="540" className="routeStreet" />)}
        {Array.from({ length: 10 }).map((_, index) => <path key={`street-h-${index}`} d={`M0 ${38 + index * 52} Q240 ${10 + index * 56}, 500 ${45 + index * 49} T1000 ${25 + index * 53}`} className="routeStreet" />)}
        <path d="M40 455 C175 420 180 290 300 270 S470 295 545 185 S770 145 965 75" className="routeAvenue" />
        <path d="M55 105 C230 150 330 105 460 155 S690 280 955 250" className="routeAvenue secondary" />
        <text x="120" y="110" className="routeDistrict">PINHEIROS</text>
        <text x="355" y="132" className="routeDistrict">JARDINS</text>
        <text x="510" y="335" className="routeDistrict">ITAIM BIBI</text>
        <text x="665" y="150" className="routeDistrict">LIBERDADE</text>
        <text x="785" y="360" className="routeDistrict">MOEMA</text>

        <g transform="translate(505 250)">
          <rect x="-25" y="-18" width="50" height="36" rx="7" className="distributionCenter" />
          <text y="4" textAnchor="middle" className="distributionText">CD</text>
          <text y="34" textAnchor="middle" className="routeMapLabel">Centro de distribuição</text>
        </g>

        {routes.map((route, index) => {
          const driver = monitoring.drivers.find(item => item.id === route.driverId)
          if (!driver) return null
          const points = [projection(driver.latitude, driver.longitude), ...route.stops.map(stop => projection(stop.latitude, stop.longitude))]
          const selected = selectedDriverId === driver.id
          const color = routeColors[index % routeColors.length]
          return (
            <g key={route.driverId} onClick={() => onSelectDriver(driver.id)} className="routeMapGroup">
              {points.length > 1 && <polyline points={points.map(point => `${point.x},${point.y}`).join(' ')} fill="none" stroke={color} strokeWidth={selected ? 6 : 4} opacity={selected ? 1 : .68} strokeLinecap="round" strokeLinejoin="round" className="routePath" style={selected ? { filter: 'url(#routeGlow)' } : undefined} />}
              {route.stops.map((stop, stopIndex) => {
                const point = projection(stop.latitude, stop.longitude)
                return <g key={`${route.driverId}-${stop.orderId}`} transform={`translate(${point.x} ${point.y})`}>
                  <circle r={selected ? 12 : 9} fill={color} stroke="#d7ecff" strokeWidth="1.5" />
                  <text y="4" textAnchor="middle" className="routeStopText">{stopIndex + 1}</text>
                </g>
              })}
              <g transform={`translate(${projection(driver.latitude, driver.longitude).x} ${projection(driver.latitude, driver.longitude).y})`}>
                <circle r={selected ? 18 : 14} fill={`${color}33`} stroke={color} strokeWidth={selected ? 3 : 2} />
                <circle r="10" fill={color} />
                <text y="4" textAnchor="middle" className="routeVehicleText">🚚</text>
                <text y="31" textAnchor="middle" className="routeMapLabel">{routeCode(driver.id)}</text>
              </g>
            </g>
          )
        })}
      </svg>

      <div className="routesLegend">
        {routes.map((route, index) => <button key={route.driverId} onClick={() => onSelectDriver(route.driverId)} className={route.driverId === selectedDriverId ? 'selected' : ''}><i style={{ background: routeColors[index % routeColors.length] }} /> {routeCode(route.driverId)} — {route.driverName}</button>)}
      </div>
    </div>
  )
}

export function Routes() {
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [routes, setRoutes] = useState<DriverRoute[]>([])
  const [selectedDriverId, setSelectedDriverId] = useState<number | null>(null)
  const [optimization, setOptimization] = useState<RouteOptimization | null>(null)
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    const monitoringResponse = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
    const data = monitoringResponse.data
    setMonitoring(data)

    const routeResults = await Promise.all(data.drivers.map(async driver => {
      const response = await api.get<DriverRoute>(`/api/dispatch/drivers/${driver.id}/route`)
      return response.data
    }))

    setRoutes(routeResults)
    setSelectedDriverId(current => current ?? routeResults.find(route => route.totalStops > 0)?.driverId ?? routeResults[0]?.driverId ?? null)
  }, [])

  useEffect(() => {
    load().catch(() => setMessage('Não foi possível carregar o centro de rotas.'))
    const timer = window.setInterval(() => load().catch(() => undefined), 15000)
    return () => window.clearInterval(timer)
  }, [load])

  const selectedRoute = routes.find(route => route.driverId === selectedDriverId) ?? null
  const selectedDriver = monitoring?.drivers.find(driver => driver.id === selectedDriverId) ?? null

  const selectedOrders = useMemo(() => {
    if (!monitoring || !selectedRoute) return []
    const byId = new Map(monitoring.orders.map(order => [order.id, order]))
    return selectedRoute.stops.map(stop => ({ stop, order: byId.get(stop.orderId) ?? null }))
  }, [monitoring, selectedRoute])

  async function generatePlan() {
    setBusy(true)
    try {
      const response = await api.post<DispatchAssignment[]>('/api/dispatch/auto-plan')
      setMessage(response.data.length ? `${response.data.length} entrega(s) foram alocadas automaticamente.` : 'Todos os pedidos elegíveis já possuem motorista.')
      setOptimization(null)
      await load()
    } catch {
      setMessage('Não foi possível gerar o plano automático de rotas.')
    } finally {
      setBusy(false)
    }
  }

  async function optimizeSelected() {
    if (!selectedDriverId) return
    setBusy(true)
    try {
      const response = await api.post<RouteOptimization>(`/api/dispatch/drivers/${selectedDriverId}/route/optimize`)
      setOptimization(response.data)
      setMessage('Nova sequência calculada pelo motor de otimização.')
    } catch {
      setMessage('Não foi possível otimizar esta rota.')
    } finally {
      setBusy(false)
    }
  }

  async function applyOptimization() {
    if (!selectedDriverId) return
    setBusy(true)
    try {
      await api.post<DriverRoute>(`/api/dispatch/drivers/${selectedDriverId}/route/apply`)
      setMessage('Nova sequência aplicada à rota com sucesso.')
      setOptimization(null)
      await load()
    } catch {
      setMessage('Não foi possível aplicar a rota otimizada.')
    } finally {
      setBusy(false)
    }
  }

  if (!monitoring) {
    return <article className="panel empty"><RefreshCcw className="spin" /><h2>Carregando rotas...</h2></article>
  }

  const activeRoutes = routes.filter(route => route.totalStops > 0)
  const totalStops = activeRoutes.reduce((sum, route) => sum + route.totalStops, 0)
  const totalDistance = activeRoutes.reduce((sum, route) => sum + Number(route.totalDistanceKm), 0)
  const totalMinutes = activeRoutes.reduce((sum, route) => sum + route.estimatedRouteMinutes, 0)
  const riskCount = monitoring.orders.filter(order => order.riskPercent >= 70).length
  const averageRisk = monitoring.orders.length ? monitoring.orders.reduce((sum, order) => sum + order.riskPercent, 0) / monitoring.orders.length : 0
  const efficiency = Math.max(0, 100 - averageRisk * .35)
  const riskyOrders = [...monitoring.orders].filter(order => order.riskPercent >= 60).sort((a,b) => b.riskPercent - a.riskPercent)

  const metrics = [
    ['Rotas ativas', activeRoutes.length, RouteIcon],
    ['Entregas previstas', totalStops, Package],
    ['Motoristas ativos', monitoring.activeDrivers, UsersRound],
    ['Distância total', `${fmt(totalDistance)} km`, Navigation],
    ['Tempo estimado', `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m`, Clock3],
    ['Eficiência', `${fmt(efficiency)}%`, Gauge],
    ['Risco de atraso', riskCount, AlertTriangle]
  ] as const

  return (
    <div className="routesPage">
      <section className="routesHeader">
        <div><div className="routesTitleLine"><h1>Centro de Rotas</h1><span><i /> ao vivo</span></div><p>Planeje, acompanhe, detecte riscos e otimize a última milha.</p></div>
        <div className="routesHeaderActions"><button onClick={() => load()}><RefreshCcw size={16}/> Atualizar</button><button className="primary" onClick={generatePlan} disabled={busy}><WandSparkles size={16}/> Gerar plano automático</button></div>
      </section>

      {message && <div className="routeNotice">{message}</div>}

      <section className="routeMetrics">
        {metrics.map(([label,value,Icon]) => <article key={label}><Icon size={20}/><div><span>{label}</span><strong>{value}</strong></div></article>)}
      </section>

      <section className="routesWorkspace">
        <article className="routesMapPanel">
          <div className="routesMapHeader"><div><strong>Mapa operacional</strong><span>Posições GPS e sequências calculadas pelo Smart Dispatch</span></div><div className="trafficStatus"><TrafficCone size={15}/> Trânsito externo: integração pendente</div></div>
          <RouteMap monitoring={monitoring} routes={activeRoutes.length ? activeRoutes : routes} selectedDriverId={selectedDriverId} onSelectDriver={driverId => { setSelectedDriverId(driverId); setOptimization(null) }} />
        </article>

        <aside className="routeSequencePanel">
          {selectedRoute && selectedDriver ? <>
            <div className="routeSequenceHeader"><div><span>{routeCode(selectedDriver.id)}</span><h2>{selectedDriver.name}</h2><small>{selectedDriver.vehicleModel} • {selectedDriver.vehiclePlate}</small></div><Truck size={24}/></div>
            <div className="routeDriverStats"><span><small>Entregas</small><b>{selectedRoute.totalStops}</b></span><span><small>Distância</small><b>{fmt(Number(selectedRoute.totalDistanceKm),2)} km</b></span><span><small>Tempo</small><b>{selectedRoute.estimatedRouteMinutes} min</b></span><span><small>Carga</small><b>{selectedDriver.currentLoad}/{selectedDriver.maxCapacity}</b></span></div>

            <button className="optimizeRouteButton" onClick={optimizeSelected} disabled={busy}><Sparkles size={17}/> Otimizar rota</button>

            {optimization && <div className="optimizationPreview"><div className="optimizationHeadline"><Sparkles size={17}/><strong>Nova rota encontrada</strong></div><div className="optimizationCompare"><span><small>Distância atual</small><b>{fmt(Number(optimization.currentDistanceKm),2)} km</b></span><span><small>Otimizada</small><b>{fmt(Number(optimization.optimizedDistanceKm),2)} km</b></span><span><small>Economia</small><b>{fmt(Number(optimization.savedDistanceKm),2)} km</b></span><span><small>Tempo salvo</small><b>{optimization.savedMinutes} min</b></span></div><button onClick={applyOptimization} disabled={busy}><CheckCircle2 size={16}/> Aplicar nova rota</button></div>}

            <div className="routeStopsList">
              <h3>Sequência das entregas</h3>
              {selectedOrders.length ? selectedOrders.map(({ stop, order }, index) => <div className="routeStopRow" key={stop.orderId}><div className="routeStopIndex">{String(index + 1).padStart(2,'0')}</div><div className="routeStopBody"><div><strong>{order?.customerName ?? `Pedido ${stop.orderNumber}`}</strong><span>#{stop.orderNumber}</span></div><p><MapPin size={13}/>{order?.destinationLabel ?? 'Destino cadastrado'}</p><small>{stop.distanceFromPreviousKm.toFixed(2)} km • ETA +{stop.etaFromNowMinutes} min • P{stop.priority}</small></div>{order && order.riskPercent >= 70 && <span className="routeRisk">{order.riskPercent}%</span>}</div>) : <div className="routeEmpty">Nenhuma entrega atribuída a este motorista.</div>}
            </div>
          </> : <div className="routeEmpty">Selecione uma rota no mapa.</div>}
        </aside>
      </section>

      <section className="routeBottomGrid">
        <article className="routeInfoCard"><div className="routeCardTitle"><AlertTriangle size={18}/><div><h3>Entregas em risco</h3><p>Detecção preventiva por ETA, SLA, carga e prioridade</p></div></div><div className="riskList">{riskyOrders.length ? riskyOrders.slice(0,5).map(order => <button key={order.id} onClick={() => order.driverId && setSelectedDriverId(order.driverId)}><span className={`riskLevel ${order.riskLevel.toLowerCase()}`}>{order.riskPercent}%</span><div><strong>#{order.orderNumber} — {order.customerName}</strong><span>{order.riskReason}</span><small>{order.driverName ?? 'Sem motorista'} • SLA {order.slaMinutes} min • ETA {order.etaMinutes ?? '—'} min</small></div></button>) : <div className="routeHealthy"><CheckCircle2 size={18}/> Nenhuma entrega com risco relevante.</div>}</div></article>

        <article className="routeInfoCard"><div className="routeCardTitle"><TrafficCone size={18}/><div><h3>Trânsito e ETA</h3><p>Camada preparada para integração com provedor externo</p></div></div><div className="trafficPanel"><div className="trafficRow warning"><i/><div><strong>ETA operacional</strong><span>Hoje calculado por distância, carga e velocidade urbana média.</span></div></div><div className="trafficRow info"><i/><div><strong>Próxima integração</strong><span>Google Routes, Mapbox, HERE ou TomTom poderão fornecer trânsito viário real.</span></div></div><div className="trafficRow success"><i/><div><strong>Reotimização automática</strong><span>O backend já consegue recalcular a sequência quando GPS, SLA ou prioridade mudarem.</span></div></div></div></article>
      </section>
    </div>
  )
}
