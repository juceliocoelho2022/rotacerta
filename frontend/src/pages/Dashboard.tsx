import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock3,
  MapPinned,
  Package,
  RefreshCcw,
  Route,
  Sparkles,
  Truck,
  UsersRound,
  Zap
} from 'lucide-react'
import { api, type OperationsMonitoring } from '../services/api'
import { StatusBadge } from '../components/StatusBadge'

function pct(value: number, total: number) {
  return total === 0 ? 0 : Math.round((value / total) * 100)
}

export function Dashboard() {
  const navigate = useNavigate()
  const [monitoring, setMonitoring] = useState<OperationsMonitoring | null>(null)
  const [message, setMessage] = useState('')
  const [planning, setPlanning] = useState(false)

  const load = useCallback(async () => {
    try {
      const response = await api.get<OperationsMonitoring>('/api/dispatch/monitoring')
      setMonitoring(response.data)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar os indicadores operacionais.')
    }
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, 15000)
    return () => window.clearInterval(timer)
  }, [load])

  const topRisk = useMemo(() => {
    if (!monitoring) return []
    return [...monitoring.orders]
      .filter(order => order.riskPercent >= 60)
      .sort((a, b) => b.riskPercent - a.riskPercent)
      .slice(0, 4)
  }, [monitoring])

  async function autoPlan() {
    setPlanning(true)
    try {
      const response = await api.post('/api/dispatch/auto-plan')
      const total = Array.isArray(response.data) ? response.data.length : 0
      setMessage(total ? `${total} entrega(s) alocadas pelo Smart Dispatch.` : 'Nenhuma entrega pendente de alocação.')
      await load()
    } catch {
      setMessage('Não foi possível executar o planejamento automático.')
    } finally {
      setPlanning(false)
    }
  }

  if (!monitoring) {
    return <article className="panel empty"><Activity className="spin" /><h2>Carregando visão executiva...</h2></article>
  }

  const failed = monitoring.orders.filter(order => order.status === 'DELIVERY_FAILED').length
  const outForDelivery = monitoring.orders.filter(order => order.status === 'OUT_FOR_DELIVERY').length
  const unassigned = monitoring.orders.filter(order => !order.driverId && ['READY_FOR_SHIPMENT', 'SHIPPED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(order.status)).length
  const highPriority = monitoring.orders.filter(order => order.priority >= 5 && order.status !== 'DELIVERED').length

  const metrics = [
    { label: 'Entregas Hoje', value: monitoring.totalOrders, meta: 'volume total', icon: Package, tone: 'blue' },
    { label: 'Em Andamento', value: monitoring.inProgress, meta: `${pct(monitoring.inProgress, monitoring.totalOrders)}% da operação`, icon: Route, tone: 'indigo' },
    { label: 'Saiu p/ entrega', value: outForDelivery, meta: 'última milha', icon: Truck, tone: 'blue' },
    { label: 'Entregues', value: monitoring.delivered, meta: `${pct(monitoring.delivered, monitoring.totalOrders)}% do total`, icon: CheckCircle2, tone: 'green' },
    { label: 'Risco alto', value: topRisk.length, meta: 'exigem atenção', icon: AlertTriangle, tone: 'red' },
    { label: 'Motoristas ativos', value: monitoring.activeDrivers, meta: `${monitoring.drivers.length} cadastrados`, icon: UsersRound, tone: 'amber' }
  ]

  return (
    <div className="opsDashboard executiveDashboard">
      <section className="opsTitleBar">
        <div>
          <div className="opsTitleLine"><h1>Visão Geral da Operação</h1><span className="liveBadge"><span className="healthDot" /> Ao vivo</span></div>
          <p>Indicadores executivos do RotaCerta, atualizados automaticamente a cada 15 segundos.</p>
        </div>
        <div className="executiveActions">
          <button className="refreshButton" onClick={load}><RefreshCcw size={16} /> Atualizar</button>
          <button className="dispatchButton" onClick={autoPlan} disabled={planning}><Sparkles size={16} /> {planning ? 'Planejando...' : 'Smart Dispatch'}</button>
        </div>
      </section>

      {message && <div className="deliveryNotice">{message}</div>}

      <section className="opsMetrics">
        {metrics.map(({ label, value, meta, icon: Icon, tone }) => (
          <article className="opsMetric" key={label}>
            <div className={`opsMetricIcon ${tone}`}><Icon size={22} /></div>
            <div><span>{label}</span><strong>{value}</strong><small>{meta}</small></div>
          </article>
        ))}
      </section>

      <section className="executiveGrid">
        <article className="opsCard executiveFlow">
          <div className="opsCardHeader"><div><h3>Fluxo operacional</h3><p>Distribuição dos pedidos por estágio</p></div><Activity size={18} /></div>
          <div className="flowBars">
            {[
              ['Em andamento', monitoring.inProgress, '#1689ff'],
              ['Entregues', monitoring.delivered, '#31c86b'],
              ['Falhas', failed, '#ef5350'],
              ['Sem motorista', unassigned, '#f2a93b']
            ].map(([label, value, color]) => (
              <div key={String(label)}><span><b>{label}</b><em>{value}</em></span><div><i style={{ width: `${pct(Number(value), monitoring.totalOrders)}%`, background: String(color) }} /></div></div>
            ))}
          </div>
        </article>

        <article className="opsCard executiveHealth">
          <div className="opsCardHeader"><div><h3>Saúde logística</h3><p>SLA, risco e capacidade</p></div><Zap size={18} /></div>
          <div className="healthScore"><strong>{monitoring.successRate.toFixed(1)}%</strong><span>taxa de sucesso</span></div>
          <div className="healthMiniGrid"><span><small>Atrasadas</small><b>{monitoring.delayed}</b></span><span><small>Prioridade máxima</small><b>{highPriority}</b></span><span><small>Sem alocação</small><b>{unassigned}</b></span><span><small>Motoristas</small><b>{monitoring.activeDrivers}</b></span></div>
        </article>

        <article className="opsCard executiveShortcuts">
          <div className="opsCardHeader"><div><h3>Centrais operacionais</h3><p>Acesse as áreas de decisão</p></div></div>
          <button onClick={() => navigate('/deliveries')}><Truck size={19} /><div><strong>Entregas</strong><span>Gerenciamento individual, SLA e intervenção</span></div></button>
          <button onClick={() => navigate('/routes')}><MapPinned size={19} /><div><strong>Rotas</strong><span>Planejamento, otimização e sequenciamento</span></div></button>
          <button onClick={() => navigate('/drivers')}><UsersRound size={19} /><div><strong>Motoristas</strong><span>Carga, disponibilidade e desempenho</span></div></button>
        </article>
      </section>

      <section className="executiveBottomGrid">
        <article className="opsCard">
          <div className="opsCardHeader"><div><h3>Entregas recentes</h3><p>Últimos pedidos da operação</p></div><Clock3 size={18} /></div>
          <div className="recentList executiveRecent">
            {monitoring.orders.slice(0, 6).map(order => (
              <button key={order.id} className="recentRow" onClick={() => navigate('/deliveries')}>
                <span className="recentCode">#{order.orderNumber}</span><span className="recentCustomer">{order.customerName}</span><StatusBadge status={order.status} /><span className="recentDriver">{order.driverName ?? 'Sem motorista'}</span>
              </button>
            ))}
          </div>
        </article>

        <article className="opsCard">
          <div className="opsCardHeader"><div><h3>Entregas em risco</h3><p>Prevenção antes do atraso</p></div><AlertTriangle size={18} /></div>
          <div className="alertList">
            {topRisk.length ? topRisk.map(order => (
              <button key={order.id} className={`alertItem ${order.riskPercent >= 90 ? 'danger' : 'warning'}`} onClick={() => navigate('/deliveries')}>
                <AlertTriangle size={17} /><div><strong>#{order.orderNumber} • risco {order.riskPercent}%</strong><span>{order.riskReason}</span></div>
              </button>
            )) : <div className="alertItem success"><CheckCircle2 size={17} /><div><strong>Operação estável</strong><span>Nenhuma entrega apresenta risco relevante.</span></div></div>}
          </div>
        </article>
      </section>
    </div>
  )
}
