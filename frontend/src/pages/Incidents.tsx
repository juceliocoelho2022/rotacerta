import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, CheckCircle2, CircleAlert, Plus, RefreshCw, Search, ShieldAlert } from 'lucide-react'
import { api } from '../services/api'

type Incident = {
  id: number
  orderId: number | null
  orderNumber: string | null
  driverId: number | null
  driverName: string | null
  vehicleId: number | null
  vehiclePlate: string | null
  severity: string
  status: string
  category: string
  title: string
  description: string
  location: string | null
  resolution: string | null
  openedAt: string
  resolvedAt: string | null
}

type RefItem = { id: number; name?: string; orderNumber?: string; plate?: string }

const severityLabels: Record<string, string> = { LOW: 'Baixa', MEDIUM: 'Média', HIGH: 'Alta', CRITICAL: 'Crítica' }
const statusLabels: Record<string, string> = { OPEN: 'Aberta', IN_PROGRESS: 'Em tratamento', RESOLVED: 'Resolvida', CLOSED: 'Encerrada' }

export function Incidents() {
  const [incidents, setIncidents] = useState<Incident[]>([])
  const [orders, setOrders] = useState<RefItem[]>([])
  const [drivers, setDrivers] = useState<RefItem[]>([])
  const [vehicles, setVehicles] = useState<RefItem[]>([])
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [form, setForm] = useState({ orderId: '', driverId: '', vehicleId: '', severity: 'MEDIUM', category: 'OPERATIONAL', title: '', description: '', location: '' })

  async function load() {
    try {
      const [incidentResponse, orderResponse, driverResponse, vehicleResponse] = await Promise.all([
        api.get<Incident[]>('/api/incidents'),
        api.get<any[]>('/api/orders'),
        api.get<any[]>('/api/drivers'),
        api.get<any[]>('/api/vehicles')
      ])
      setIncidents(incidentResponse.data)
      setOrders(orderResponse.data)
      setDrivers(driverResponse.data)
      setVehicles(vehicleResponse.data)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar as ocorrências.')
    }
  }

  useEffect(() => { void load() }, [])

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    return incidents.filter(incident => {
      const matchesStatus = statusFilter === 'ALL' || incident.status === statusFilter
      const matchesTerm = !term || [incident.title, incident.description, incident.orderNumber ?? '', incident.driverName ?? '', incident.vehiclePlate ?? '', incident.category]
        .some(value => value.toLowerCase().includes(term))
      return matchesStatus && matchesTerm
    })
  }, [incidents, search, statusFilter])

  const stats = useMemo(() => ({
    open: incidents.filter(i => i.status === 'OPEN' || i.status === 'IN_PROGRESS').length,
    critical: incidents.filter(i => i.severity === 'CRITICAL' && i.status !== 'RESOLVED' && i.status !== 'CLOSED').length,
    resolved: incidents.filter(i => i.status === 'RESOLVED' || i.status === 'CLOSED').length,
    total: incidents.length
  }), [incidents])

  async function createIncident(event: React.FormEvent) {
    event.preventDefault()
    try {
      await api.post('/api/incidents', {
        orderId: form.orderId ? Number(form.orderId) : null,
        driverId: form.driverId ? Number(form.driverId) : null,
        vehicleId: form.vehicleId ? Number(form.vehicleId) : null,
        severity: form.severity,
        category: form.category,
        title: form.title,
        description: form.description,
        location: form.location || null
      })
      setForm({ orderId: '', driverId: '', vehicleId: '', severity: 'MEDIUM', category: 'OPERATIONAL', title: '', description: '', location: '' })
      setShowForm(false)
      await load()
      setMessage('Ocorrência registrada com sucesso.')
    } catch (error: any) {
      setMessage(error?.response?.data?.detail ?? 'Não foi possível registrar a ocorrência.')
    }
  }

  async function updateStatus(incident: Incident, status: string) {
    let resolution: string | null = incident.resolution
    if (status === 'RESOLVED' || status === 'CLOSED') {
      resolution = window.prompt('Informe a resolução da ocorrência:', incident.resolution ?? '')
      if (!resolution?.trim()) return
    }
    try {
      const response = await api.patch<Incident>(`/api/incidents/${incident.id}/status`, { status, resolution })
      setIncidents(current => current.map(item => item.id === incident.id ? response.data : item))
      setMessage('Ocorrência atualizada.')
    } catch (error: any) {
      setMessage(error?.response?.data?.detail ?? 'Não foi possível atualizar a ocorrência.')
    }
  }

  return (
    <section className="opsSuitePage">
      <header className="opsSuiteHeader">
        <div><span className="opsSuiteEyebrow">CENTRO DE EXCEÇÕES</span><h1>Ocorrências</h1><p>Registro, priorização e tratamento de eventos que impactam a operação logística.</p></div>
        <div className="opsSuiteHeaderActions"><button className="opsButton secondary" onClick={() => void load()}><RefreshCw size={16} /> Atualizar</button><button className="opsButton primary" onClick={() => setShowForm(value => !value)}><Plus size={16} /> Nova ocorrência</button></div>
      </header>

      <div className="opsKpiGrid">
        <article className="opsKpi"><CircleAlert /><span>Em aberto</span><strong>{stats.open}</strong></article>
        <article className="opsKpi danger"><ShieldAlert /><span>Críticas</span><strong>{stats.critical}</strong></article>
        <article className="opsKpi"><CheckCircle2 /><span>Resolvidas</span><strong>{stats.resolved}</strong></article>
        <article className="opsKpi"><AlertTriangle /><span>Total</span><strong>{stats.total}</strong></article>
      </div>

      {showForm && <form className="opsFormPanel" onSubmit={createIncident}>
        <div className="opsFormTitle"><AlertTriangle size={18} /><strong>Registrar ocorrência</strong></div>
        <div className="opsFormGrid">
          <label>Severidade<select value={form.severity} onChange={e => setForm({ ...form, severity: e.target.value })}><option value="LOW">Baixa</option><option value="MEDIUM">Média</option><option value="HIGH">Alta</option><option value="CRITICAL">Crítica</option></select></label>
          <label>Categoria<select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}><option value="OPERATIONAL">Operacional</option><option value="VEHICLE">Veículo</option><option value="DELIVERY">Entrega</option><option value="CUSTOMER">Cliente</option><option value="SAFETY">Segurança</option><option value="SYSTEM">Sistema</option></select></label>
          <label>Pedido<select value={form.orderId} onChange={e => setForm({ ...form, orderId: e.target.value })}><option value="">Sem vínculo</option>{orders.map(order => <option key={order.id} value={order.id}>{order.orderNumber}</option>)}</select></label>
          <label>Motorista<select value={form.driverId} onChange={e => setForm({ ...form, driverId: e.target.value })}><option value="">Sem vínculo</option>{drivers.map(driver => <option key={driver.id} value={driver.id}>{driver.name}</option>)}</select></label>
          <label>Veículo<select value={form.vehicleId} onChange={e => setForm({ ...form, vehicleId: e.target.value })}><option value="">Sem vínculo</option>{vehicles.map(vehicle => <option key={vehicle.id} value={vehicle.id}>{vehicle.plate}</option>)}</select></label>
          <label>Local<input value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} placeholder="Centro Operacional / cidade" /></label>
          <label className="opsSpan2">Título<input required maxLength={140} value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="Resumo objetivo da ocorrência" /></label>
          <label className="opsSpan2">Descrição<textarea required rows={3} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Descreva impacto, contexto e ação inicial..." /></label>
        </div>
        <div className="opsFormActions"><button type="button" className="opsButton secondary" onClick={() => setShowForm(false)}>Cancelar</button><button className="opsButton primary">Registrar</button></div>
      </form>}

      {message && <div className="opsMessage">{message}</div>}

      <article className="opsTablePanel">
        <div className="opsToolbar">
          <div className="opsSearch"><Search size={16} /><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Buscar ocorrência..." /></div>
          <select className="opsFilter" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}><option value="ALL">Todos os status</option>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select>
        </div>
        <div className="opsIncidentList">
          {filtered.map(incident => <article className={`opsIncident severity-${incident.severity.toLowerCase()}`} key={incident.id}>
            <div className="opsIncidentHead"><div><span className={`opsBadge severity-${incident.severity.toLowerCase()}`}>{severityLabels[incident.severity]}</span><span className="opsBadge neutral">{incident.category}</span></div><select className="opsInlineSelect" value={incident.status} onChange={e => void updateStatus(incident, e.target.value)}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div>
            <h3>{incident.title}</h3><p>{incident.description}</p>
            <div className="opsIncidentRefs"><span>#{incident.id}</span>{incident.orderNumber && <span>Pedido {incident.orderNumber}</span>}{incident.driverName && <span>{incident.driverName}</span>}{incident.vehiclePlate && <span>{incident.vehiclePlate}</span>}{incident.location && <span>{incident.location}</span>}<span>{new Date(incident.openedAt).toLocaleString('pt-BR')}</span></div>
            {incident.resolution && <div className="opsResolution"><strong>Resolução:</strong> {incident.resolution}</div>}
          </article>)}
          {!filtered.length && <div className="opsEmpty">Nenhuma ocorrência encontrada.</div>}
        </div>
      </article>
    </section>
  )
}
