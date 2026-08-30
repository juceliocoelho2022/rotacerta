import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  AlertTriangle,
  BatteryCharging,
  CheckCircle2,
  FileCheck2,
  Gauge,
  History,
  LockKeyhole,
  MapPin,
  PackageCheck,
  Plane,
  RadioTower,
  RefreshCcw,
  Route,
  ShieldAlert,
  ShieldCheck,
  UserCheck
} from 'lucide-react'
import {
  api,
  type Drone,
  type DroneAuthorization,
  type DroneAuthorizationDecision,
  type DroneAuditCheck,
  type DroneEligibility,
  type DroneMission,
  type DroneMissionStatus
} from '../services/api'
import { DroneMissionTimeline } from '../components/DroneMissionTimeline'

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

const CHECK_LABEL: Record<DroneAuditCheck, string> = {
  PASSED: 'Validado',
  FAILED: 'Falhou',
  PENDING_EXTERNAL: 'Pendente externo'
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

export function Drones() {
  const [drones, setDrones] = useState<Drone[]>([])
  const [missions, setMissions] = useState<DroneMission[]>([])
  const [authorizations, setAuthorizations] = useState<DroneAuthorization[]>([])
  const [orderId, setOrderId] = useState('')
  const [eligibility, setEligibility] = useState<DroneEligibility | null>(null)
  const [selectedMissionId, setSelectedMissionId] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [authorizationLoading, setAuthorizationLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [authorizedBy, setAuthorizedBy] = useState('Administrador RotaCerta')
  const [authorizationReason, setAuthorizationReason] = useState('')
  const [validMinutes, setValidMinutes] = useState(60)
  const [evidenceType, setEvidenceType] = useState('CHECKLIST_OPERACIONAL')
  const [evidenceReference, setEvidenceReference] = useState('')
  const [evidenceDescription, setEvidenceDescription] = useState('')

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

  const loadAuthorizations = useCallback(async (missionId: number) => {
    setAuthorizationLoading(true)
    try {
      const response = await api.get<DroneAuthorization[]>(`/api/drone-delivery/missions/${missionId}/authorizations`)
      setAuthorizations(response.data)
    } catch {
      setAuthorizations([])
      setMessage('Não foi possível carregar o histórico de autorização da missão.')
    } finally {
      setAuthorizationLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, 15000)
    return () => window.clearInterval(timer)
  }, [load])

  useEffect(() => {
    if (selectedMissionId) {
      loadAuthorizations(selectedMissionId)
    } else {
      setAuthorizations([])
    }
  }, [selectedMissionId, loadAuthorizations])

  const selectedMission = missions.find(mission => mission.id === selectedMissionId) ?? null
  const available = drones.filter(drone => drone.available && drone.status === 'AVAILABLE').length
  const inFlight = drones.filter(drone => ['IN_FLIGHT', 'RETURNING'].includes(drone.status)).length
  const charging = drones.filter(drone => drone.status === 'CHARGING').length
  const averageBattery = drones.length ? Math.round(drones.reduce((sum, drone) => sum + drone.batteryPercent, 0) / drones.length) : 0
  const activeMissions = missions.filter(mission => !['COMPLETED', 'ABORTED'].includes(mission.status)).length
  const latestAuthorization = authorizations[0] ?? null
  const activeAuthorization = authorizations.find(item => item.decision === 'APPROVED_SIMULATION' && item.active) ?? null

  const nextStatus = useMemo(() => {
    if (!selectedMission || selectedMission.status === 'ABORTED' || selectedMission.status === 'PLANNED') return null
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
      setMessage(`Missão ${response.data.id} criada para o drone ${response.data.droneCode}. Agora registre a decisão de autorização.`)
      setEligibility(null)
      await load()
      setSelectedMissionId(response.data.id)
      await loadAuthorizations(response.data.id)
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Não foi possível criar a missão.')
    } finally {
      setActionLoading(false)
    }
  }

  async function registerAuthorization(decision: DroneAuthorizationDecision) {
    if (!selectedMission) return
    if (!authorizedBy.trim()) {
      setMessage('Informe quem está registrando a decisão.')
      return
    }
    if (!authorizationReason.trim()) {
      setMessage('Informe a justificativa da decisão para manter a trilha auditável.')
      return
    }
    if (validMinutes < 5 || validMinutes > 240) {
      setMessage('A validade deve ficar entre 5 e 240 minutos.')
      return
    }

    const evidenceStarted = Boolean(evidenceReference.trim() || evidenceDescription.trim())
    if (decision === 'APPROVED_SIMULATION' && !evidenceReference.trim()) {
      setMessage('Para aprovar a simulação, registre ao menos uma evidência com referência.')
      return
    }
    if (evidenceStarted && (!evidenceType.trim() || !evidenceReference.trim())) {
      setMessage('Para registrar evidência, informe tipo e referência.')
      return
    }

    const evidence = evidenceReference.trim()
      ? [{
          evidenceType: evidenceType.trim(),
          reference: evidenceReference.trim(),
          description: evidenceDescription.trim() || null
        }]
      : []

    setActionLoading(true)
    setMessage('')
    try {
      const response = await api.post<DroneAuthorization>(
        `/api/drone-delivery/missions/${selectedMission.id}/authorizations`,
        {
          decision,
          authorizedBy: authorizedBy.trim(),
          reason: authorizationReason.trim(),
          validMinutes,
          evidence
        }
      )
      setMessage(decision === 'APPROVED_SIMULATION'
        ? `Autorização simulada #${response.data.id} registrada com validade auditável.`
        : `Decisão de rejeição #${response.data.id} registrada na trilha de auditoria.`)
      setAuthorizationReason('')
      setEvidenceReference('')
      setEvidenceDescription('')
      await load()
      await loadAuthorizations(selectedMission.id)
      setSelectedMissionId(selectedMission.id)
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Não foi possível registrar a autorização.')
    } finally {
      setActionLoading(false)
    }
  }

  async function advanceMission() {
    if (!selectedMission || !nextStatus) return
    if (selectedMission.status === 'AUTHORIZED' && !activeAuthorization) {
      setMessage('A autorização não está ativa. Registre ou renove a decisão antes de iniciar o carregamento.')
      return
    }
    setActionLoading(true)
    try {
      await api.patch(`/api/drone-delivery/missions/${selectedMission.id}/status`, { status: nextStatus })
      setMessage(`Missão ${selectedMission.id} avançou para ${STATUS_LABEL[nextStatus]}.`)
      await load()
      await loadAuthorizations(selectedMission.id)
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
      await loadAuthorizations(selectedMission.id)
      setSelectedMissionId(selectedMission.id)
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Não foi possível abortar a missão.')
    } finally {
      setActionLoading(false)
    }
  }

  function checkChip(label: string, check: DroneAuditCheck) {
    return (
      <span className={`droneAuditCheck ${check.toLowerCase()}`}>
        <b>{label}</b>
        <small>{CHECK_LABEL[check]}</small>
      </span>
    )
  }

  const showAuthorizationForm = selectedMission && (
    selectedMission.status === 'PLANNED'
    || (selectedMission.status === 'AUTHORIZED' && !activeAuthorization)
  )

  return (
    <div className="dronePage">
      <section className="droneHeader">
        <div>
          <span>ROTA AÉREA · PROTÓTIPO OPERACIONAL</span>
          <h1>Drone Delivery</h1>
          <p>Elegibilidade, frota aérea, missões e autorização auditável integradas aos pedidos do RotaCerta.</p>
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
          <div className="dronePanelTitle"><div><strong>Missões</strong><span>Fluxo com decisão auditável antes do carregamento.</span></div></div>
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

              <DroneMissionTimeline missionId={selectedMission.id} refreshKey={selectedMission.updatedAt} />

              <section className="droneAuthorizationPanel">
                <div className="droneAuthorizationTitle">
                  <LockKeyhole size={16}/>
                  <div><strong>Authorization Audit Trail</strong><span>Quem decidiu, contexto validado, validade e evidências.</span></div>
                  {authorizationLoading && <small>carregando...</small>}
                </div>

                {latestAuthorization ? (
                  <div className={`droneAuthorizationSummary ${latestAuthorization.decision === 'APPROVED_SIMULATION' ? 'approved' : 'rejected'}`}>
                    <div className="droneAuthorizationDecision">
                      {latestAuthorization.decision === 'APPROVED_SIMULATION' ? <UserCheck size={16}/> : <ShieldAlert size={16}/>} 
                      <span>
                        <strong>{latestAuthorization.decision === 'APPROVED_SIMULATION' ? 'Aprovada para simulação' : 'Rejeitada'}</strong>
                        <small>{latestAuthorization.active ? 'Autorização ativa' : 'Histórico / não ativa'}</small>
                      </span>
                    </div>
                    <div className="droneAuthorizationMeta">
                      <span><small>Responsável</small><b>{latestAuthorization.authorizedBy}</b></span>
                      <span><small>Decisão</small><b>{formatDateTime(latestAuthorization.authorizedAt)}</b></span>
                      <span><small>Válida até</small><b>{formatDateTime(latestAuthorization.validUntil)}</b></span>
                      <span><small>Política</small><b>{latestAuthorization.policyVersion}</b></span>
                    </div>
                    <p>{latestAuthorization.reason}</p>
                    <div className="droneAuditChecks">
                      {checkChip('Carga', latestAuthorization.payloadCheck)}
                      {checkChip('Bateria', latestAuthorization.batteryCheck)}
                      {checkChip('Rota', latestAuthorization.routeCheck)}
                      {checkChip('Espaço aéreo', latestAuthorization.airspaceCheck)}
                      {checkChip('Meteorologia', latestAuthorization.weatherCheck)}
                      {checkChip('Geofencing', latestAuthorization.geofenceCheck)}
                    </div>
                    <div className="droneFingerprint"><FileCheck2 size={13}/><span>SHA-256</span><code>{latestAuthorization.contextFingerprint.slice(0, 18)}…</code></div>
                    {latestAuthorization.evidence.length > 0 && (
                      <div className="droneEvidenceList">
                        <strong>Evidências registradas</strong>
                        {latestAuthorization.evidence.map(item => (
                          <span key={item.id}><b>{item.evidenceType}</b><small>{item.reference}{item.description ? ` · ${item.description}` : ''}</small></span>
                        ))}
                      </div>
                    )}
                    <details className="droneContextSnapshot">
                      <summary>Ver snapshot imutável do contexto</summary>
                      <pre>{JSON.stringify(JSON.parse(latestAuthorization.contextSnapshot), null, 2)}</pre>
                    </details>
                  </div>
                ) : (
                  <div className="droneAuthorizationEmpty"><History size={16}/> Nenhuma decisão registrada para esta missão.</div>
                )}

                {showAuthorizationForm && (
                  <div className="droneAuthorizationForm">
                    <div className="droneAuthorizationWarning">
                      <ShieldAlert size={15}/>
                      <span><strong>Decisão de simulação, não autorização regulatória real.</strong><small>ANAC, DECEA, ANATEL, meteorologia e geofencing permanecem como integrações externas pendentes.</small></span>
                    </div>
                    <label>Responsável pela decisão<input value={authorizedBy} onChange={event => setAuthorizedBy(event.target.value)} maxLength={160}/><small>Identificação declarada; autenticação forte de operador é uma evolução futura.</small></label>
                    <label>Justificativa<textarea value={authorizationReason} onChange={event => setAuthorizationReason(event.target.value)} maxLength={1000} placeholder="Descreva o contexto e o motivo da decisão." /></label>
                    <label>Validade da decisão<select value={validMinutes} onChange={event => setValidMinutes(Number(event.target.value))}><option value={15}>15 minutos</option><option value={30}>30 minutos</option><option value={60}>60 minutos</option><option value={120}>120 minutos</option><option value={240}>240 minutos</option></select></label>
                    <div className="droneEvidenceForm">
                      <strong>Evidência obrigatória para aprovação</strong>
                      <input value={evidenceType} onChange={event => setEvidenceType(event.target.value)} placeholder="Tipo: CHECKLIST_OPERACIONAL" />
                      <input value={evidenceReference} onChange={event => setEvidenceReference(event.target.value)} placeholder="Referência, protocolo, hash ou URL interna" />
                      <textarea value={evidenceDescription} onChange={event => setEvidenceDescription(event.target.value)} placeholder="Descrição da evidência" />
                    </div>
                    <div className="droneAuthorizationActions">
                      <button className="approve" onClick={() => registerAuthorization('APPROVED_SIMULATION')} disabled={actionLoading}><ShieldCheck size={15}/> {selectedMission.status === 'AUTHORIZED' ? 'Renovar autorização' : 'Aprovar simulação'}</button>
                      {selectedMission.status === 'PLANNED' && <button className="reject" onClick={() => registerAuthorization('REJECTED')} disabled={actionLoading}><ShieldAlert size={15}/> Rejeitar</button>}
                    </div>
                  </div>
                )}

                {authorizations.length > 1 && (
                  <div className="droneAuthorizationHistory">
                    <strong><History size={13}/> Histórico de decisões</strong>
                    {authorizations.slice(1).map(item => (
                      <span key={item.id}><b>#{item.id} · {item.decision === 'APPROVED_SIMULATION' ? 'APROVADA' : 'REJEITADA'}</b><small>{item.authorizedBy} · {formatDateTime(item.authorizedAt)} · até {formatDateTime(item.validUntil)}</small></span>
                    ))}
                  </div>
                )}
              </section>

              <div className="droneMissionActions">
                {nextStatus && <button className="primary" onClick={advanceMission} disabled={actionLoading || (selectedMission.status === 'AUTHORIZED' && !activeAuthorization)}>Avançar → {STATUS_LABEL[nextStatus]}</button>}
                {!['COMPLETED','ABORTED'].includes(selectedMission.status) && <button className="danger" onClick={abortMission} disabled={actionLoading}>Abortar missão</button>}
              </div>
            </div>
          )}
        </aside>
      </section>
    </div>
  )
}
