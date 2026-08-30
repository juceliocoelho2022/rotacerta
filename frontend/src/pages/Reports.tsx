import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, BarChart3, Boxes, CheckCircle2, RefreshCw, Truck, UsersRound } from 'lucide-react'
import { api } from '../services/api'

type Report = {
  totalOrders: number
  deliveredOrders: number
  inProgressOrders: number
  failedOrders: number
  deliverySuccessRate: number
  deliveredRevenue: number
  totalDrivers: number
  activeDrivers: number
  totalVehicles: number
  availableVehicles: number
  maintenanceVehicles: number
  totalDrones: number
  availableDrones: number
  openIncidents: number
  criticalIncidents: number
  ordersByStatus: Record<string, number>
}

const statusLabels: Record<string, string> = {
  ORDER_CREATED: 'Criado', PAYMENT_APPROVED: 'Pagamento aprovado', PICKING: 'Separação', PACKING: 'Embalagem',
  READY_FOR_SHIPMENT: 'Pronto para envio', SHIPPED: 'Enviado', IN_TRANSIT: 'Em trânsito', OUT_FOR_DELIVERY: 'Saiu para entrega',
  DELIVERED: 'Entregue', DELIVERY_FAILED: 'Falha', RETURNED: 'Devolvido', CANCELLED: 'Cancelado'
}

export function Reports() {
  const [report, setReport] = useState<Report | null>(null)
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  async function load() {
    setLoading(true)
    try {
      const response = await api.get<Report>('/api/reports/operations')
      setReport(response.data)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar os indicadores.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void load() }, [])

  const statusEntries = useMemo(() => {
    if (!report) return []
    return Object.entries(report.ordersByStatus).filter(([, count]) => count > 0)
  }, [report])

  if (loading && !report) return <section className="opsSuitePage"><div className="opsEmpty">Carregando relatório operacional...</div></section>

  return (
    <section className="opsSuitePage">
      <header className="opsSuiteHeader">
        <div><span className="opsSuiteEyebrow">ANALYTICS OPERACIONAL</span><h1>Relatórios</h1><p>Indicadores consolidados de pedidos, frota, motoristas, drones e ocorrências.</p></div>
        <button className="opsButton secondary" onClick={() => void load()}><RefreshCw size={16} /> Atualizar indicadores</button>
      </header>

      {message && <div className="opsMessage">{message}</div>}
      {report && <>
        <div className="opsKpiGrid">
          <article className="opsKpi"><Boxes /><span>Pedidos</span><strong>{report.totalOrders}</strong><small>{report.inProgressOrders} em andamento</small></article>
          <article className="opsKpi"><CheckCircle2 /><span>Taxa de sucesso</span><strong>{report.deliverySuccessRate.toLocaleString('pt-BR')}%</strong><small>{report.deliveredOrders} entregues</small></article>
          <article className="opsKpi"><BarChart3 /><span>Receita entregue</span><strong>{Number(report.deliveredRevenue).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</strong><small>Pedidos concluídos</small></article>
          <article className={`opsKpi ${report.criticalIncidents > 0 ? 'danger' : ''}`}><AlertTriangle /><span>Ocorrências abertas</span><strong>{report.openIncidents}</strong><small>{report.criticalIncidents} crítica(s)</small></article>
        </div>

        <div className="opsReportGrid">
          <article className="opsReportPanel">
            <div className="opsPanelHeading"><div><span className="opsSuiteEyebrow">PEDIDOS</span><h2>Distribuição por status</h2></div><span>{report.totalOrders} registros</span></div>
            <div className="opsStatusBars">
              {statusEntries.map(([status, count]) => {
                const width = report.totalOrders === 0 ? 0 : Math.max(4, (count / report.totalOrders) * 100)
                return <div className="opsStatusRow" key={status}><div><span>{statusLabels[status] ?? status}</span><strong>{count}</strong></div><div className="opsBarTrack"><span style={{ width: `${width}%` }} /></div></div>
              })}
            </div>
          </article>

          <article className="opsReportPanel">
            <div className="opsPanelHeading"><div><span className="opsSuiteEyebrow">CAPACIDADE</span><h2>Recursos operacionais</h2></div></div>
            <div className="opsResourceGrid">
              <div><UsersRound /><span>Motoristas</span><strong>{report.activeDrivers}/{report.totalDrivers}</strong><small>disponíveis</small></div>
              <div><Truck /><span>Veículos</span><strong>{report.availableVehicles}/{report.totalVehicles}</strong><small>{report.maintenanceVehicles} em manutenção</small></div>
              <div><span className="opsDroneGlyph">✣</span><span>Drones</span><strong>{report.availableDrones}/{report.totalDrones}</strong><small>disponíveis</small></div>
              <div><AlertTriangle /><span>Falhas terminais</span><strong>{report.failedOrders}</strong><small>falha, devolução ou cancelamento</small></div>
            </div>
          </article>
        </div>

        <article className="opsTablePanel">
          <div className="opsPanelHeading"><div><span className="opsSuiteEyebrow">RESUMO EXECUTIVO</span><h2>Leitura rápida da operação</h2></div></div>
          <div className="opsExecutiveSummary">
            <div><strong>{report.deliveredOrders}</strong><span>entregas concluídas</span></div>
            <div><strong>{report.inProgressOrders}</strong><span>pedidos ainda em fluxo</span></div>
            <div><strong>{report.activeDrivers}</strong><span>motoristas disponíveis</span></div>
            <div><strong>{report.availableVehicles}</strong><span>veículos disponíveis</span></div>
            <div><strong>{report.availableDrones}</strong><span>drones disponíveis</span></div>
            <div><strong>{report.openIncidents}</strong><span>ocorrências demandando atenção</span></div>
          </div>
        </article>
      </>}
    </section>
  )
}
