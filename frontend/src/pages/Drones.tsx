import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AlertTriangle,
  BatteryCharging,
  CheckCircle2,
  Gauge,
  MapPin,
  PackageCheck,
  Plane,
  RadioTower,
  RefreshCcw,
  Route,
  ShieldCheck
} from 'lucide-react'
import { api, type Drone, type DroneEligibility, type DroneMission, type DroneMissionStatus } from '../services/api'

const FLOW: DroneMissionStatus[] = [
  'PLANNED',
  'AUTHORIZED',
  'LOADING',
  'READY_FOR_TAKEOFF',
  'IN_FLIGHT',
  'APPROACHING',
  'LOWERING_PACKAGE',
  'DELIVERED',
  'RETURNING',
  'COMPLETED'
]

const STATUS_LABEL: Record<DroneMissionStatus, string> = {
  PLANNED: 'Planejada',
  AUTHORIZED: 'Autorizada (simulação)',
  LOADING: 'Carregando pacote',
  READY_FOR_TAKEOFF: 'Pronta para decolar',
  IN_FLIGHT: 'Em voo',
  APPROACHING: 'Aproximando',
  LOWERING_PACKAGE: 'Baixando pacote',
  DELIVERED: 'Entregue',
  RETURNING: 'Retornando',
  COMPLETED: 'Concluída',
  ABORTED: 'Abortada'
}

export function Drones() {
  const [drones, setDrones] = useState<Drone[]>([])
  const [missions, setMissions] = useState<DroneMission[]>([])
  const [orderId, setOrderId] = useState('')
  const [eligibility, setEligibility] = useState<DroneEligibility | null>(null)
  const [selectedMissionId, setSelectedMissionId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [message, setMessage] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [droneResponse, missionResponse] = await Promise.all([
        api.get<Drone[]>('/api/drone-delivery/drones'),
        api.get<DroneMission[]>('/api/drone-delivery/missions')
      ])
      setDrones(droneResponse.data)
      setMissions(missionResponse.data)
      setSelectedMissionId(current => current && missionResponse.data.some(m => m.id === current)
        ? current
        : missionResponse.data[0]?.id ?? null)
    } catch {
      setMessage('Não foi possível carregar a operação de drones.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, 15000)
    return () => window.clearInterval(timer)
  }, [load])

  const selectedMission = missions.find(mission => mission.id === selectedMissionId) ?? null
  const available = drones.filter(drone => drone.available && drone.status === 'AVAILABLE').length
  const inFlight = drones.filter(drone => ['IN_FLIGHT', 'RETURNING'].includes(drone.status)).length
  const charging = drones.filter(drone => drone.status === 'CHARGING').length
  const averageBattery = drones.length ? Math.round(drones.reduce((sum, drone) => sum + drone.batteryPercent, 0) / drones.length) : 0
  const activeMissions = missions.filter(mission => !['COMPLETED', 'ABORTED'].includes(mission.status)).length

  const nextStatus = useMemo(() => {
    if (!selectedMission || selectedMission.status === 'ABORTED') return null
    const index = FLOW.indexOf(selectedMission.status)
    return index >= 0 && index < FLOW.length - 1 ? FLOW[index + 1] : null
  }, [selectedMission])

  async function analyzeOrder() {
    const parsed = Number(orderId)
    if (!Number.isInteger(parsed) || parsed < 1) {
      setMessage('Informe um ID de pedido válido.')
      return
    }
    setActionLoading(true)
    setMessage('')
    try {
      const response = await api.get<DroneEligibility>(`/api/drone-delivery/orders/${parsed}/eligibility`)
      setEligibility(response.data)
    } catch (error: any) {
      setEligibility(null)
      setMessage(error?.response?.data?.message ?? 'Não foi possível analisar o pedido.')
    } finally {
      setActionLoading(false)
    }
  }

  async function createMission() {
    if (!eligibility?.eligible) return
    setActionLoading(true)
    try {
      const response = await api.post<DroneMission>(`/api/drone-delivery/orders/${eligibility.orderId}/missions`)
      setMessage(`Missão ${response.data.id} criada para o drone ${response.data.droneCode}.`)
      setEligibility(null)
      await load()
      setSelectedMissionId(response.data.id)
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Não foi possível criar a missão.')
    } finally {
      setActionLoading(false)
    }
  }

  async function advanceMission() {
    if (!selectedMission || !nextStatus) return
    setActionLoading(true)
    try {
      await api.patch(`/api/drone-delivery/missions/${selectedMission.id}/status`, { status: nextStatus })
      setMessage(`Missão ${selectedMission.id} avançou para ${STATUS_LABEL[nextStatus]}.`)
      await load()
      setSelectedMissionId(selectedMission.id)
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Não foi possível avançar a missão.')
    } finally {
      setActionLoading(false)
    }
  }

  async function abortMission() {
    if (!selectedMission || ['COMPLETED', 'ABORTED'].includes(selectedMission.status)) return
    setActionLoading(true)
    try {
      await api.patch(`/api/drone-delivery/missions/${selectedMission.id}/status`, { status: 'ABORTED' })
      setMessage(`Missão ${selectedMission.id} abortada no simulador.`)
      await load()
      setSelectedMissionId(selectedMission.id)
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <div className="dronePage">
      <section className="droneHeader">
        <div>
          <span>ROTA AÉREA · PROTÓTIPO OPERACIONAL</span>
          <h1>Drone Delivery</h1>
          <p>Elegibilidade, frota aérea e missões simuladas integradas aos pedidos do RotaCerta.</p>
        </div>
        <button onClick={load} disabled={loading}><RefreshCcw className={loading ? 'spin' : ''} size={16}/> Atualizar</button>
      </section>

      <div className="droneSafetyBanner">
        <ShieldCheck size={18}/>
        <div><strong>Modo de simulação</strong><span>O sistema não comanda hardware real nem substitui autorizações da ANAC, DECEA, ANATEL, meteorologia, geofencing ou avaliação de zona segura.</span></div>
      </div>

      {message && <div className="droneMessage">{message}</div>}

      <section className="droneMetrics">
        <article><Plane/><span><small>Drones</small><strong>{drones.length}</strong></span></article>
        <article><CheckCircle2/><span><small>Disponíveis</small><strong>{available}</strong></span></article>
        <article><RadioTower/><span><small>Em voo/retorno</small><strong>{inFlight}</strong></span></article>
        <article><BatteryCharging/><span><small>Carregando</small><strong>{charging}</strong></span></article>
        <article><PackageCheck/><span><small>Missões ativas</small><strong>{activeMissions}</strong></span></article>
        <article><Gauge/><span><small>Bateria média</small><strong>{averageBattery}%</strong></span></article>
      </section>

      <section className="droneWorkspace">
        <div className="droneMainColumn">
          <article className="droneEligibilityPanel">
            <div className="dronePanelTitle"><div><strong>Analisar pedido para drone</strong><span>Use um pedido em READY_FOR_SHIPMENT com peso e coordenadas.</span></div></div>
            <div className="droneOrderAnalyzer">
              <input value={orderId} onChange={event => setOrderId(event.target.value)} inputMode="numeric" placeholder="ID do pedido, ex.: 7" />
              <button onClick={analyzeOrder} disabled={actionLoading}>Analisar elegibilidade</button>
            </div>

            {eligibility && (
              <div className={`droneEligibilityResult ${eligibility.eligible ? 'eligible' : 'blocked'}`}>
                <div className="droneEligibilityHeadline">
                  {eligibility.eligible ? <CheckCircle2/> : <AlertTriangle/>}
                  <div><strong>{eligibility.eligible ? 'Elegível internamente para simulação' : 'Não elegível'}</strong><span>Pedido #{eligibility.orderNumber} · {eligibility.payloadKg.toFixed(3)} kg</span></div>
                </div>
                <div className="droneEligibilityGrid">
                  <div><small>Drone recomendado</small><strong>{eligibility.recommendedDroneCode ?? '—'}</strong></div>
                  <div><small>Distância aérea</small><strong>{eligibility.estimatedDistanceKm != null ? `${eligibility.estimatedDistanceKm.toFixed(2)} km` : '—'}</strong></div>
                  <div><small>ETA estimado</small><strong>{eligibility.estimatedEtaMinutes != null ? `${eligibility.estimatedEtaMinutes} min` : '—'}</strong></div>
                  <div><small>Destino</small><strong>{eligibility.destinationLatitude.toFixed(4)}, {eligibility.destinationLongitude.toFixed(4)}</strong></div>
                </div>
                {eligibility.blockers.length > 0 && <div className="droneBlockers"><strong>Bloqueios</strong>{eligibility.blockers.map(item => <span key={item}>• {item}</span>)}</div>}
                <div className="droneExternalChecks"><strong>Checagens externas ainda pendentes</strong>{eligibility.pendingExternalChecks.map(item => <span key={item}>• {item}</span>)}</div>
                {eligibility.eligible && <button className="droneCreateMission" onClick={createMission} disabled={actionLoading}><Plane size={16}/> Criar missão simulada</button>}
              </div>
            )}
          </article>

          <article className="droneFleetPanel">
            <div className="dronePanelTitle"><div><strong>Frota aérea</strong><span>Capacidade, alcance, bateria e estado operacional.</span></div></div>
            <div className="droneFleetGrid">
              {drones.map(drone => (
                <div className="droneCard" key={drone.id}>
                  <div className="droneCardTop"><span className="droneIcon"><Plane size={18}/></span><div><strong>{drone.code}</strong><small>{drone.model}</small></div><i className={`droneState ${drone.status.toLowerCase()}`}>{drone.status.replaceAll('_', ' ')}</i></div>
                  <div className="droneCardStats"><span><small>Bateria</small><b>{drone.batteryPercent}%</b></span><span><small>Carga máx.</small><b>{drone.maxPayloadKg} kg</b></span><span><small>Alcance</small><b>{drone.maxRangeKm} km</b></span></div>
                  <div className="droneBattery"><i style={{width:`${drone.batteryPercent}%`}} /></div>
                  <p><MapPin size={13}/> {drone.latitude.toFixed(5)}, {drone.longitude.toFixed(5)}</p>
                </div>
              ))}
            </div>
          </article>
        </div>

        <aside className="droneMissionPanel">
          <div className="dronePanelTitle"><div><strong>Missões</strong><span>Fluxo de voo controlado pelo simulador.</span></div></div>
          <div className="droneMissionList">
            {missions.length ? missions.map(mission => (
              <button key={mission.id} className={selectedMissionId === mission.id ? 'active' : ''} onClick={() => setSelectedMissionId(mission.id)}>
                <span><strong>MISS-{String(mission.id).padStart(4,'0')}</strong><small>Pedido #{mission.orderNumber}</small></span>
                <i>{STATUS_LABEL[mission.status]}</i>
              </button>
            )) : <div className="droneEmpty">Nenhuma missão criada.</div>}
          </div>

          {selectedMission && (
            <div className="droneMissionDetail">
              <div className="droneMissionHero"><Plane size={24}/><div><small>MISSÃO #{selectedMission.id}</small><h2>{selectedMission.droneCode}</h2><span>{STATUS_LABEL[selectedMission.status]}</span></div></div>
              <div className="droneMissionStats">
                <div><Route/><small>Distância</small><strong>{selectedMission.distanceKm.toFixed(2)} km</strong></div>
                <div><Gauge/><small>ETA</small><strong>{selectedMission.etaMinutes} min</strong></div>
                <div><PackageCheck/><small>Carga</small><strong>{selectedMission.payloadKg.toFixed(3)} kg</strong></div>
              </div>
              <div className="droneMissionCoordinates"><span><small>Origem</small><b>{selectedMission.originLatitude.toFixed(5)}, {selectedMission.originLongitude.toFixed(5)}</b></span><span><small>Destino</small><b>{selectedMission.destinationLatitude.toFixed(5)}, {selectedMission.destinationLongitude.toFixed(5)}</b></span></div>
              <div className="droneMissionActions">
                {nextStatus && <button className="primary" onClick={advanceMission} disabled={actionLoading}>Avançar → {STATUS_LABEL[nextStatus]}</button>}
                {!['COMPLETED','ABORTED'].includes(selectedMission.status) && <button className="danger" onClick={abortMission} disabled={actionLoading}>Abortar missão</button>}
              </div>
            </div>
          )}
        </aside>
      </section>
    </div>
  )
}
