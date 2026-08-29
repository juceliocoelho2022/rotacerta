import { FormEvent, useState } from 'react'
import { Search, MapPin, CheckCircle2 } from 'lucide-react'
import { api, type TrackingData } from '../services/api'
import { StatusBadge, statusLabels } from '../components/StatusBadge'

export function Tracking() {
  const [code, setCode] = useState('RC-2026-SP-8F29A73')
  const [data, setData] = useState<TrackingData | null>(null)
  const [error, setError] = useState('')

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setData(null)
    try {
      const response = await api.get<TrackingData>(`/api/tracking/${code}`)
      setData(response.data)
    } catch {
      setError('Código de rastreio não encontrado.')
    }
  }

  return (
    <section className="trackingGrid">
      <article className="panel">
        <div className="panelHeader"><div><h2>Rastrear pedido</h2><p>Consulte cada etapa da entrega</p></div></div>
        <form className="trackForm" onSubmit={submit}>
          <input value={code} onChange={e => setCode(e.target.value)} placeholder="RC-2026-SP-..." />
          <button><Search size={18}/> Rastrear</button>
        </form>
        {error && <p className="error">{error}</p>}
        <small>Teste: RC-2026-SP-8F29A73</small>
      </article>

      {data && (
        <article className="panel">
          <div className="panelHeader">
            <div><h2>Pedido #{data.orderNumber}</h2><p>{data.customerName}</p></div>
            <StatusBadge status={data.status}/>
          </div>
          <div className="timeline">
            {data.events.map((event, index) => (
              <div className="timelineItem" key={`${event.status}-${index}`}>
                <div className="timelineIcon"><CheckCircle2 size={18}/></div>
                <div>
                  <b>{statusLabels[event.status]}</b>
                  <span><MapPin size={14}/>{event.location ?? 'Local não informado'}</span>
                  <small>{new Date(event.eventTime).toLocaleString('pt-BR')}</small>
                </div>
              </div>
            ))}
          </div>
        </article>
      )}
    </section>
  )
}
