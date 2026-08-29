import { FormEvent, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { CheckCircle2, Clock3, MapPin, PackageCheck, ShieldCheck, Truck } from 'lucide-react'
import { api, type DeliveryStatus, type TrackingEvent } from '../services/api'
import { statusLabels } from '../components/StatusBadge'
import '../styles/live.css'

type LiveTrackingData = {
  trackingCode: string
  orderNumber: string
  customerName: string
  status: DeliveryStatus
  expiresAt: string
  alternateRecipientName: string | null
  alternateRecipientRelationship: string | null
  deliveryInstructions: string | null
  events: TrackingEvent[]
}

const relationshipOptions = ['Familiar', 'Vizinho', 'Porteiro', 'Outra pessoa']

export function LiveTracking() {
  const { token } = useParams()
  const [data, setData] = useState<LiveTrackingData | null>(null)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [name, setName] = useState('')
  const [relationship, setRelationship] = useState('Familiar')
  const [instructions, setInstructions] = useState('')

  async function load() {
    if (!token) return
    try {
      const response = await api.get<LiveTrackingData>(`/api/public/live/${token}`)
      setData(response.data)
      setError('')
    } catch {
      setError('Este link de acompanhamento é inválido, expirou ou a entrega já foi encerrada.')
    }
  }

  useEffect(() => {
    load()
    const interval = window.setInterval(load, 30_000)
    return () => window.clearInterval(interval)
  }, [token])

  async function authorizeRecipient(event: FormEvent) {
    event.preventDefault()
    if (!token) return

    setSaving(true)
    setMessage('')
    try {
      const response = await api.post<LiveTrackingData>(`/api/public/live/${token}/recipient`, {
        name,
        relationship,
        instructions
      })
      setData(response.data)
      setMessage('Pessoa autorizada com sucesso. A central de entrega já pode consultar essa instrução.')
      setName('')
      setInstructions('')
    } catch {
      setMessage('Não foi possível salvar a autorização. Confira os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  if (error) {
    return (
      <main className="livePage">
        <section className="liveErrorCard">
          <ShieldCheck size={42} />
          <h1>RotaCerta Live</h1>
          <p>{error}</p>
        </section>
      </main>
    )
  }

  if (!data) {
    return <main className="livePage"><p className="liveLoading">Carregando acompanhamento da entrega...</p></main>
  }

  return (
    <main className="livePage">
      <header className="liveHero">
        <div className="liveBrand"><span>RC</span><strong>RotaCerta Live</strong></div>
        <div className="liveHeroText">
          <span className="liveEyebrow"><Truck size={17}/> ENTREGA EM ANDAMENTO</span>
          <h1>Sua encomenda está a caminho</h1>
          <p>Acompanhe o andamento e, se não estiver no endereço, autorize outra pessoa a receber.</p>
        </div>
      </header>

      <section className="liveSummaryGrid">
        <article className="liveCard liveStatusCard">
          <div className="liveCardIcon"><PackageCheck size={24}/></div>
          <div>
            <small>Status atual</small>
            <h2>{statusLabels[data.status]}</h2>
            <p>Pedido #{data.orderNumber}</p>
          </div>
        </article>
        <article className="liveCard">
          <div className="liveCardIcon"><MapPin size={24}/></div>
          <div>
            <small>Código de rastreamento</small>
            <h2 className="liveCode">{data.trackingCode}</h2>
            <p>Cliente: {data.customerName}</p>
          </div>
        </article>
        <article className="liveCard">
          <div className="liveCardIcon"><Clock3 size={24}/></div>
          <div>
            <small>Atualização</small>
            <h2>A cada 30 segundos</h2>
            <p>O link é temporário e seguro.</p>
          </div>
        </article>
      </section>

      <section className="liveContentGrid">
        <article className="livePanel">
          <div className="livePanelHeader">
            <div><h2>Andamento da entrega</h2><p>Eventos registrados pela operação logística</p></div>
            <span className="livePulse">AO VIVO</span>
          </div>

          <div className="liveTimeline">
            {data.events.map((event, index) => (
              <div className="liveTimelineItem" key={`${event.status}-${event.eventTime}-${index}`}>
                <div className="liveTimelineDot"><CheckCircle2 size={18}/></div>
                <div>
                  <strong>{statusLabels[event.status]}</strong>
                  <span><MapPin size={14}/>{event.location ?? 'Localização não informada'}</span>
                  <small>{new Date(event.eventTime).toLocaleString('pt-BR')}</small>
                </div>
              </div>
            ))}
          </div>
        </article>

        <article className="livePanel">
          <div className="livePanelHeader">
            <div><h2>Não estará no endereço?</h2><p>Autorize previamente alguém de confiança.</p></div>
          </div>

          {data.alternateRecipientName && (
            <div className="authorizedRecipient">
              <ShieldCheck size={22}/>
              <div>
                <strong>Recebedor autorizado</strong>
                <span>{data.alternateRecipientName} • {data.alternateRecipientRelationship}</span>
                {data.deliveryInstructions && <small>{data.deliveryInstructions}</small>}
              </div>
            </div>
          )}

          <form className="liveForm" onSubmit={authorizeRecipient}>
            <label>
              Nome de quem poderá receber
              <input value={name} onChange={e => setName(e.target.value)} maxLength={120} required placeholder="Ex.: Maria Souza" />
            </label>
            <label>
              Relação com o destinatário
              <select value={relationship} onChange={e => setRelationship(e.target.value)}>
                {relationshipOptions.map(option => <option key={option}>{option}</option>)}
              </select>
            </label>
            <label>
              Instruções para a entrega
              <textarea value={instructions} onChange={e => setInstructions(e.target.value)} maxLength={500} placeholder="Ex.: entregar no apartamento 32 ou na portaria." />
            </label>
            <button disabled={saving}>{saving ? 'Salvando...' : 'Autorizar recebimento'}</button>
          </form>

          {message && <p className="liveMessage">{message}</p>}
          <p className="liveSafety"><ShieldCheck size={16}/> Não compartilhe este link publicamente. Ele dá acesso temporário às informações desta entrega.</p>
        </article>
      </section>
    </main>
  )
}
