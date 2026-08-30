import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Activity,
  Crosshair,
  Gauge,
  MapPin,
  Navigation,
  RefreshCcw,
  Route,
  Satellite,
  Timer
} from 'lucide-react'
import { api, type DroneMission, type DroneMissionStatus } from '../services/api'
import '../styles/drone-flight-control.css'

interface DroneFlightSimulation {
  missionId: number
  orderId: number
  orderNumber: string
  droneCode: string
  status: DroneMissionStatus
  mode: string
  phase: string
  progressPercent: number
  legProgressPercent: number
  currentLatitude: number | null
  currentLongitude: number | null
  originLatitude: number
  originLongitude: number
  destinationLatitude: number
  destinationLongitude: number
  totalDistanceKm: number
  remainingDistanceKm: number | null
  etaMinutes: number
  remainingEtaMinutes: number
  moving: boolean
  positionSource: string
  calculatedAt: string
}

const STATUS_LABEL: Record<DroneMissionStatus, string> = {
  PLANNED: 'Planejada',
  AUTHORIZED: 'Autorizada',
  LOADING: 'Carregando',
  READY_FOR_TAKEOFF: 'Pronta para decolar',
  IN_FLIGHT: 'Em voo',
  APPROACHING: 'Aproximando',
  LOWERING_PACKAGE: 'Baixando pacote',
  DELIVERED: 'Entregue',
  RETURNING: 'Retornando',
  COMPLETED: 'Concluída',
  ABORTED: 'Abortada'
}

const PHASE_LABEL: Record<string, string> = {
  PRE_FLIGHT: 'Pré-voo',
  OUTBOUND: 'Trecho de ida',
  DELIVERY: 'Operação de entrega',
  RETURN: 'Retorno à base',
  COMPLETED: 'Operação concluída',
  ABORTED: 'Operação abortada'
}

function formatTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date(value))
}

function projectPoint(
  latitude: number,
  longitude: number,
  minLat: number,
  maxLat: number,
  minLon: number,
  maxLon: number
) {
  const width = 640
  const height = 300
  const margin = 38
  const latSpan = Math.max(0.0001, maxLat - minLat)
  const lonSpan = Math.max(0.0001, maxLon - minLon)
  const x = margin + ((longitude - minLon) / lonSpan) * (width - margin * 2)
  const y = height - margin - ((latitude - minLat) / latSpan) * (height - margin * 2)
  return { x, y }
}

export function DroneFlightControlCenter() {
  const [missions, setMissions] = useState<DroneMission[]>([])
  const [selectedMissionId, setSelectedMissionId] = useState<number | null>(null)
  const [simulation, setSimulation] = useState<DroneFlightSimulation | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadMissions = useCallback(async () => {
    try {
      const response = await api.get<DroneMission[]>('/api/drone-delivery/missions')
      setMissions(response.data)
      setSelectedMissionId(current => {
        if (current && response.data.some(mission => mission.id === current)) return current
        return response.data[0]?.id ?? null
      })
      setError('')
    } catch {
      setError('Não foi possível carregar as missões da Central de Voo.')
    }
  }, [])

  const loadSimulation = useCallback(async (missionId: number) => {
    try {
      const response = await api.get<DroneFlightSimulation>(`/api/drone-delivery/missions/${missionId}/flight-simulation`)
      setSimulation(response.data)
      setError('')
    } catch {
      setSimulation(null)
      setError('Não foi possível calcular a posição simulada desta missão.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadMissions()
    const timer = window.setInterval(loadMissions, 15000)
    return () => window.clearInterval(timer)
  }, [loadMissions])

  useEffect(() => {
    if (!selectedMissionId) {
      setSimulation(null)
      setLoading(false)
      return
    }

    setLoading(true)
    loadSimulation(selectedMissionId)
    const timer = window.setInterval(() => loadSimulation(selectedMissionId), 5000)
    return () => window.clearInterval(timer)
  }, [selectedMissionId, loadSimulation])

  const map = useMemo(() => {
    if (!simulation) return null

    const latitudes = [simulation.originLatitude, simulation.destinationLatitude]
    const longitudes = [simulation.originLongitude, simulation.destinationLongitude]
    if (simulation.currentLatitude != null && simulation.currentLongitude != null) {
      latitudes.push(simulation.currentLatitude)
      longitudes.push(simulation.currentLongitude)
    }

    const rawMinLat = Math.min(...latitudes)
    const rawMaxLat = Math.max(...latitudes)
    const rawMinLon = Math.min(...longitudes)
    const rawMaxLon = Math.max(...longitudes)
    const latPadding = Math.max(0.002, (rawMaxLat - rawMinLat) * 0.18)
    const lonPadding = Math.max(0.002, (rawMaxLon - rawMinLon) * 0.18)

    const minLat = rawMinLat - latPadding
    const maxLat = rawMaxLat + latPadding
    const minLon = rawMinLon - lonPadding
    const maxLon = rawMaxLon + lonPadding

    const origin = projectPoint(simulation.originLatitude, simulation.originLongitude, minLat, maxLat, minLon, maxLon)
    const destination = projectPoint(simulation.destinationLatitude, simulation.destinationLongitude, minLat, maxLat, minLon, maxLon)
    const current = simulation.currentLatitude != null && simulation.currentLongitude != null
      ? projectPoint(simulation.currentLatitude, simulation.currentLongitude, minLat, maxLat, minLon, maxLon)
      : null

    return { origin, destination, current }
  }, [simulation])

  async function refresh() {
    await loadMissions()
    if (selectedMissionId) await loadSimulation(selectedMissionId)
  }

  return (
    <section className="flightControlCenter">
      <header className="flightControlHeader">
        <div>
          <span className="flightEyebrow"><Satellite size={13}/> CENTRAL DE VOO · SIMULAÇÃO OPERACIONAL</span>
          <h2>Mapa operacional da rota aérea</h2>
          <p>Posição calculada por interpolação entre origem e destino. Não representa GPS, telemetria ou autorização regulatória real.</p>
        </div>
        <div className="flightHeaderActions">
          <select
            value={selectedMissionId ?? ''}
            onChange={event => setSelectedMissionId(event.target.value ? Number(event.target.value) : null)}
            aria-label="Selecionar missão"
          >
            {missions.length === 0 && <option value="">Nenhuma missão</option>}
            {missions.map(mission => (
              <option value={mission.id} key={mission.id}>
                MISS-{String(mission.id).padStart(4, '0')} · {mission.droneCode} · {mission.orderNumber}
              </option>
            ))}
          </select>
          <button onClick={refresh} disabled={loading}><RefreshCcw size={15} className={loading ? 'spin' : ''}/> Atualizar</button>
        </div>
      </header>

      {error && <div className="flightControlError">{error}</div>}

      {!simulation ? (
        <div className="flightControlEmpty">Selecione uma missão para visualizar a simulação operacional.</div>
      ) : (
        <>
          <div className="flightTelemetryGrid">
            <article><Activity/><span><small>Status</small><strong>{STATUS_LABEL[simulation.status]}</strong><em>{PHASE_LABEL[simulation.phase] ?? simulation.phase}</em></span></article>
            <article><Gauge/><span><small>Progresso da missão</small><strong>{simulation.progressPercent.toFixed(1)}%</strong><em>Trecho {simulation.legProgressPercent.toFixed(1)}%</em></span></article>
            <article><Route/><span><small>Distância restante</small><strong>{simulation.remainingDistanceKm == null ? '—' : `${simulation.remainingDistanceKm.toFixed(2)} km`}</strong><em>Total {simulation.totalDistanceKm.toFixed(2)} km</em></span></article>
            <article><Timer/><span><small>ETA restante</small><strong>{simulation.remainingEtaMinutes} min</strong><em>Planejado {simulation.etaMinutes} min</em></span></article>
          </div>

          <div className="flightMapLayout">
            <div className="flightMapCanvas">
              <div className="flightMapBadge"><Navigation size={12}/> {simulation.mode}</div>
              <svg viewBox="0 0 640 300" role="img" aria-label="Mapa operacional simulado da missão de drone">
                <defs>
                  <pattern id={`flight-grid-${simulation.missionId}`} width="40" height="40" patternUnits="userSpaceOnUse">
                    <path d="M 40 0 L 0 0 0 40" className="flightGridLine" fill="none" />
                  </pattern>
                  <filter id={`flight-glow-${simulation.missionId}`} x="-50%" y="-50%" width="200%" height="200%">
                    <feGaussianBlur stdDeviation="4" result="blur" />
                    <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
                  </filter>
                </defs>
                <rect width="640" height="300" className="flightMapBackground" />
                <rect width="640" height="300" fill={`url(#flight-grid-${simulation.missionId})`} />

                {map && (
                  <>
                    <line x1={map.origin.x} y1={map.origin.y} x2={map.destination.x} y2={map.destination.y} className="flightRouteShadow" />
                    <line x1={map.origin.x} y1={map.origin.y} x2={map.destination.x} y2={map.destination.y} className="flightRouteLine" />

                    <g transform={`translate(${map.origin.x} ${map.origin.y})`}>
                      <circle r="11" className="flightOriginRing" />
                      <circle r="4" className="flightOriginDot" />
                      <text x="14" y="-10" className="flightMapLabel">BASE</text>
                    </g>

                    <g transform={`translate(${map.destination.x} ${map.destination.y})`}>
                      <circle r="11" className="flightDestinationRing" />
                      <circle r="4" className="flightDestinationDot" />
                      <text x="14" y="18" className="flightMapLabel">DESTINO</text>
                    </g>

                    {map.current && (
                      <g transform={`translate(${map.current.x} ${map.current.y})`} filter={`url(#flight-glow-${simulation.missionId})`}>
                        <circle r="17" className={`flightDroneHalo ${simulation.moving ? 'moving' : ''}`} />
                        <g className="flightDroneGlyph">
                          <line x1="-10" y1="-7" x2="10" y2="7" />
                          <line x1="10" y1="-7" x2="-10" y2="7" />
                          <circle cx="-11" cy="-8" r="4" />
                          <circle cx="11" cy="-8" r="4" />
                          <circle cx="-11" cy="8" r="4" />
                          <circle cx="11" cy="8" r="4" />
                          <rect x="-5" y="-4" width="10" height="8" rx="2" />
                        </g>
                      </g>
                    )}
                  </>
                )}
              </svg>
              <div className="flightMapLegend">
                <span><i className="base"/> Base</span>
                <span><i className="destination"/> Destino</span>
                <span><i className="drone"/> Posição simulada</span>
              </div>
            </div>

            <aside className="flightMissionInspector">
              <div className="flightMissionIdentity">
                <Crosshair size={18}/>
                <span><small>MISS-{String(simulation.missionId).padStart(4, '0')}</small><strong>{simulation.droneCode}</strong><em>Pedido {simulation.orderNumber}</em></span>
              </div>

              <div className="flightProgressBlock">
                <div><span>Progresso operacional</span><strong>{simulation.progressPercent.toFixed(1)}%</strong></div>
                <div className="flightProgressTrack"><i style={{ width: `${Math.max(0, Math.min(100, simulation.progressPercent))}%` }} /></div>
              </div>

              <div className="flightCoordinateList">
                <span><MapPin size={12}/><div><small>Origem</small><b>{simulation.originLatitude.toFixed(5)}, {simulation.originLongitude.toFixed(5)}</b></div></span>
                <span><Navigation size={12}/><div><small>Posição simulada</small><b>{simulation.currentLatitude == null ? 'Indisponível' : `${simulation.currentLatitude.toFixed(5)}, ${simulation.currentLongitude?.toFixed(5)}`}</b></div></span>
                <span><MapPin size={12}/><div><small>Destino</small><b>{simulation.destinationLatitude.toFixed(5)}, {simulation.destinationLongitude.toFixed(5)}</b></div></span>
              </div>

              <div className="flightSimulationMeta">
                <span><small>Fonte da posição</small><b>{simulation.positionSource}</b></span>
                <span><small>Movimento</small><b>{simulation.moving ? 'SIMULADO EM ANDAMENTO' : 'SEM MOVIMENTO SIMULADO'}</b></span>
                <span><small>Último cálculo</small><b>{formatTime(simulation.calculatedAt)}</b></span>
              </div>
            </aside>
          </div>
        </>
      )}
    </section>
  )
}
