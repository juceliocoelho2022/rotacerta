import { useCallback, useEffect, useState } from 'react'
import { Clock3, History } from 'lucide-react'
import { api, type DroneMissionStatus } from '../services/api'
import '../styles/drone-timeline.css'

interface DroneMissionEvent {
  id: number
  eventType: string
  missionStatus: DroneMissionStatus | null
  title: string
  description: string | null
  actor: string | null
  createdAt: string
}

interface Props {
  missionId: number
  refreshKey?: string
}

const STATUS_LABEL: Partial<Record<DroneMissionStatus, string>> = {
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

function formatTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date(value))
}

function formatFullDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date(value))
}

export function DroneMissionTimeline({ missionId, refreshKey }: Props) {
  const [events, setEvents] = useState<DroneMissionEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  const loadTimeline = useCallback(async () => {
    try {
      const response = await api.get<DroneMissionEvent[]>(`/api/drone-delivery/missions/${missionId}/timeline`)
      setEvents(response.data)
      setError(false)
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }, [missionId])

  useEffect(() => {
    setLoading(true)
    loadTimeline()
    const timer = window.setInterval(loadTimeline, 15000)
    return () => window.clearInterval(timer)
  }, [loadTimeline, refreshKey])

  return (
    <section className="droneTimeline">
      <div className="droneTimelineHeader">
        <div>
          <History size={15}/>
          <span>
            <strong>Histórico da missão</strong>
            <small>Timeline operacional auditável do voo simulado.</small>
          </span>
        </div>
        {loading && <small>atualizando...</small>}
      </div>

      <div className="droneTimelineBody">
        {error ? (
          <div className="droneTimelineEmpty">Não foi possível carregar a timeline desta missão.</div>
        ) : events.length === 0 ? (
          <div className="droneTimelineEmpty">Nenhum evento operacional registrado.</div>
        ) : events.map(event => (
          <div
            className={`droneTimelineItem ${event.eventType.toLowerCase()}`}
            key={event.id}
            title={formatFullDate(event.createdAt)}
          >
            <time className="droneTimelineTime">{formatTime(event.createdAt)}</time>
            <span className="droneTimelineMarker" />
            <div className="droneTimelineContent">
              <strong>{event.title}</strong>
              {event.description && <p>{event.description}</p>}
              <div className="droneTimelineMeta">
                {event.missionStatus && <span className="status">{STATUS_LABEL[event.missionStatus] ?? event.missionStatus}</span>}
                {event.eventType === 'STATUS_SNAPSHOT' && <span>Snapshot histórico</span>}
                {event.actor && <span className="actor">{event.actor}</span>}
                <span><Clock3 size={9}/> {formatFullDate(event.createdAt)}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
