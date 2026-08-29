import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  CalendarDays,
  Eye,
  Mail,
  MapPin,
  MoreHorizontal,
  PackageCheck,
  Pencil,
  Phone,
  Plus,
  RefreshCcw,
  Search,
  Star,
  UserCheck,
  UserPlus,
  UsersRound
} from 'lucide-react'
import { CustomerFormModal, type CustomerFormMode } from '../components/CustomerFormModal'
import { PreferenceFormModal } from '../components/PreferenceFormModal'
import { StatusBadge } from '../components/StatusBadge'
import {
  api,
  type CustomerDetail,
  type CustomerListItem,
  type CustomerOrder
} from '../services/api'

type CustomerTab = 'summary' | 'addresses' | 'history' | 'preferences' | 'occurrences'

const terminalStatuses = new Set(['DELIVERED', 'DELIVERY_FAILED', 'RETURNED', 'CANCELLED'])

function formatDate(value: string | null) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(new Date(value))
}

function formatDateTime(value: string | null) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function formatMoney(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

function initials(name: string) {
  return name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0]?.toUpperCase()).join('')
}

function CustomerOrders({ orders }: { orders: CustomerOrder[] }) {
  if (!orders.length) return <div className="customerEmpty">Nenhum pedido registrado para este cliente.</div>
  return (
    <div className="customerOrderHistory">
      {orders.map(order => (
        <div className="customerOrderRow" key={order.id}>
          <div><strong>#{order.orderNumber}</strong><span>{formatDateTime(order.createdAt)}</span></div>
          <StatusBadge status={order.status} />
          <b>{formatMoney(order.total)}</b>
        </div>
      ))}
    </div>
  )
}

export function Customers() {
  const navigate = useNavigate()
  const [customers, setCustomers] = useState<CustomerListItem[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [detail, setDetail] = useState<CustomerDetail | null>(null)
  const [tab, setTab] = useState<CustomerTab>('summary')
  const [formMode, setFormMode] = useState<CustomerFormMode | null>(null)
  const [preferenceOpen, setPreferenceOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('ALL')
  const [city, setCity] = useState('ALL')
  const [deliveryFilter, setDeliveryFilter] = useState('ALL')
  const [occurrenceFilter, setOccurrenceFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  const loadCustomers = useCallback(async () => {
    setLoading(true)
    try {
      const response = await api.get<CustomerListItem[]>('/api/customers')
      setCustomers(response.data)
      setSelectedId(current => current ?? response.data[0]?.id ?? null)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar os clientes.')
    } finally {
      setLoading(false)
    }
  }, [])

  const loadDetail = useCallback(async (id: number) => {
    const response = await api.get<CustomerDetail>(`/api/customers/${id}`)
    setDetail(response.data)
  }, [])

  useEffect(() => { loadCustomers() }, [loadCustomers])

  useEffect(() => {
    if (!selectedId) {
      setDetail(null)
      return
    }
    loadDetail(selectedId).catch(() => setMessage('Não foi possível carregar os detalhes do cliente.'))
  }, [loadDetail, selectedId])

  async function handleSaved(customerId: number) {
    const savedMode = formMode
    setSelectedId(customerId)
    await loadCustomers()
    await loadDetail(customerId)
    setFormMode(null)
    if (savedMode === 'address') setTab('addresses')
    if (savedMode === 'recipient') setTab('summary')
    setMessage(savedMode === 'customer' ? 'Cliente cadastrado com sucesso.' : savedMode === 'address' ? 'Endereço adicionado com sucesso.' : 'Recebedor autorizado cadastrado com sucesso.')
  }

  async function handlePreferenceSaved(customerId: number) {
    await loadDetail(customerId)
    setPreferenceOpen(false)
    setTab('preferences')
    setMessage('Preferências de entrega atualizadas com sucesso.')
  }

  const cities = useMemo(
    () => [...new Set(customers.map(customer => customer.city).filter((value): value is string => Boolean(value)))].sort(),
    [customers]
  )

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    return customers.filter(customer => {
      const matchesSearch = !term || customer.name.toLowerCase().includes(term) || customer.email.toLowerCase().includes(term) || customer.phone?.toLowerCase().includes(term)
      const matchesStatus = status === 'ALL' || (status === 'ACTIVE' ? customer.active : !customer.active)
      const matchesCity = city === 'ALL' || customer.city === city
      const matchesDelivery = deliveryFilter === 'ALL' || (deliveryFilter === 'YES' ? customer.activeDeliveries > 0 : customer.activeDeliveries === 0)
      const matchesOccurrence = occurrenceFilter === 'ALL' || (occurrenceFilter === 'YES' ? customer.occurrences > 0 : customer.occurrences === 0)
      return matchesSearch && matchesStatus && matchesCity && matchesDelivery && matchesOccurrence
    })
  }, [city, customers, deliveryFilter, occurrenceFilter, search, status])

  const activeCount = customers.filter(customer => customer.active).length
  const withDelivery = customers.filter(customer => customer.activeDeliveries > 0).length
  const withOccurrences = customers.filter(customer => customer.occurrences > 0).length
  const averageRating = customers.length ? customers.reduce((sum, customer) => sum + Number(customer.rating), 0) / customers.length : 0
  const now = new Date()
  const newThisMonth = customers.filter(customer => {
    const created = new Date(customer.createdAt)
    return created.getMonth() === now.getMonth() && created.getFullYear() === now.getFullYear()
  }).length

  const currentOrder = detail?.orders.find(order => !terminalStatuses.has(order.status)) ?? null
  const occurrenceOrders = detail?.orders.filter(order => ['DELIVERY_FAILED', 'RETURNED'].includes(order.status)) ?? []

  const cards = [
    ['Clientes cadastrados', customers.length, UsersRound],
    ['Clientes ativos', activeCount, UserCheck],
    ['Com entrega em andamento', withDelivery, PackageCheck],
    ['Satisfação média', averageRating.toFixed(1).replace('.', ','), Star],
    ['Com ocorrência', withOccurrences, AlertTriangle],
    ['Novos no mês', newThisMonth, UserPlus]
  ] as const

  return (
    <div className="customersPage">
      <section className="customersHeader">
        <div><h1>Clientes</h1><p>Gerencie seus clientes, endereços, preferências e histórico de entregas.</p></div>
        <button className="refreshButton" onClick={loadCustomers}><RefreshCcw size={16} /> Atualizar</button>
      </section>

      {message && <div className="customerNotice">{message}</div>}

      <section className="customerMetrics">
        {cards.map(([label, value, Icon]) => (
          <article key={label}><div className="customerMetricIcon"><Icon size={20} /></div><div><span>{label}</span><strong>{value}</strong></div></article>
        ))}
      </section>

      <section className="customerToolbar">
        <label className="customerSearch"><Search size={16} /><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Buscar cliente..." /></label>
        <select value={status} onChange={event => setStatus(event.target.value)}><option value="ALL">Status</option><option value="ACTIVE">Ativos</option><option value="INACTIVE">Inativos</option></select>
        <select value={city} onChange={event => setCity(event.target.value)}><option value="ALL">Cidade</option>{cities.map(item => <option value={item} key={item}>{item}</option>)}</select>
        <select value={deliveryFilter} onChange={event => setDeliveryFilter(event.target.value)}><option value="ALL">Com entrega</option><option value="YES">Sim</option><option value="NO">Não</option></select>
        <select value={occurrenceFilter} onChange={event => setOccurrenceFilter(event.target.value)}><option value="ALL">Ocorrências</option><option value="YES">Com ocorrência</option><option value="NO">Sem ocorrência</option></select>
        <button className="customerPrimaryButton" onClick={() => setFormMode('customer')}><Plus size={16} /> Novo cliente</button>
      </section>

      <section className="customerWorkspace">
        <article className="customerTablePanel">
          <div className="customerTableHeader"><strong>{filtered.length} cliente(s)</strong><span>Dados operacionais do backend</span></div>
          <div className="tableWrap">
            <table className="customerTable">
              <thead><tr><th>Cliente</th><th>Contato</th><th>Cidade</th><th>Entregas</th><th>Em andamento</th><th>Último pedido</th><th>Avaliação</th><th>Ações</th></tr></thead>
              <tbody>
                {filtered.map(customer => (
                  <tr key={customer.id} className={selectedId === customer.id ? 'selected' : ''} onClick={() => { setSelectedId(customer.id); setTab('summary') }}>
                    <td><div className="customerIdentity"><span>{initials(customer.name)}</span><div><strong>{customer.name}</strong><small className={customer.active ? 'active' : 'inactive'}>{customer.active ? 'Ativo' : 'Inativo'}</small></div></div></td>
                    <td><strong>{customer.phone ?? '—'}</strong><small>{customer.email}</small></td>
                    <td>{customer.city ? `${customer.city} / ${customer.state}` : '—'}</td>
                    <td><strong>{customer.totalOrders}</strong><small>{formatMoney(customer.totalSpent)}</small></td>
                    <td><strong>{customer.activeDeliveries}</strong>{customer.activeDeliveries > 0 && <small className="linkish">Ver pedidos</small>}</td>
                    <td>{formatDate(customer.lastOrderAt)}</td>
                    <td><div className="ratingCell"><strong>{Number(customer.rating).toFixed(1).replace('.', ',')}</strong><Star size={14} /></div></td>
                    <td><button className="customerRowAction" onClick={event => { event.stopPropagation(); setSelectedId(customer.id) }}><Eye size={15} /></button><button className="customerRowAction" onClick={event => event.stopPropagation()}><MoreHorizontal size={15} /></button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {loading && <div className="customerLoading"><RefreshCcw className="spin" /> Carregando clientes...</div>}
        </article>

        <aside className="customerDrawer">
          {detail ? (
            <>
              <div className="customerDrawerTop"><div><h2>{detail.name}</h2><span className={detail.active ? 'customerState active' : 'customerState inactive'}>{detail.active ? '● Ativo' : '● Inativo'}</span></div></div>

              <div className="customerTabs">
                {([['summary', 'Resumo'], ['addresses', 'Endereços'], ['history', 'Histórico'], ['preferences', 'Preferências'], ['occurrences', 'Ocorrências']] as const).map(([value, label]) => <button className={tab === value ? 'active' : ''} key={value} onClick={() => setTab(value)}>{label}</button>)}
              </div>

              {tab === 'summary' && (
                <div className="customerTabContent">
                  <section className="customerProfileGrid">
                    <div className="customerAvatarLarge">{initials(detail.name)}</div>
                    <div className="customerContacts"><span><Phone size={14} /> {detail.phone ?? 'Telefone não informado'}</span><span><Mail size={14} /> {detail.email}</span><span><CalendarDays size={14} /> Cliente desde {formatDate(detail.createdAt)}</span></div>
                    <div className="customerRatingBox"><small>Satisfação</small><strong>{Number(detail.rating).toFixed(1).replace('.', ',')} <Star size={17} /></strong></div>
                  </section>

                  <section className="customerCurrentDelivery">
                    <div className="sectionTitle"><strong>Entrega em andamento</strong>{currentOrder && <span>Em operação</span>}</div>
                    {currentOrder ? <><div className="currentDeliveryBody"><div><strong>Pedido #{currentOrder.orderNumber}</strong><span>{formatMoney(currentOrder.total)}</span></div><StatusBadge status={currentOrder.status} /></div><button onClick={() => navigate('/tracking')}>Acompanhar entrega</button></> : <div className="customerEmpty">Nenhuma entrega em andamento.</div>}
                  </section>

                  <section className="customerTwoColumns">
                    <div className="customerMiniPanel">
                      <div className="sectionTitle"><strong>Endereços de entrega</strong><button onClick={() => setTab('addresses')}>Ver todos</button></div>
                      {detail.addresses.slice(0, 3).map(address => <div className="addressMini" key={address.id}><MapPin size={15}/><div><strong>{address.label}{address.primaryAddress ? ' • Principal' : ''}</strong><span>{address.street}, {address.number}</span><small>{address.city}/{address.state}</small></div></div>)}
                      {!detail.addresses.length && <div className="customerEmpty">Nenhum endereço cadastrado.</div>}
                    </div>
                    <div className="customerMiniPanel">
                      <div className="sectionTitle"><strong>Pessoas autorizadas</strong><button onClick={() => setFormMode('recipient')}><Plus size={14}/> Adicionar</button></div>
                      {detail.authorizedRecipients.slice(0, 3).map(recipient => <div className="recipientMini" key={recipient.id}><UserCheck size={15}/><div><strong>{recipient.name}</strong><span>{recipient.relationship}</span></div><small>{recipient.active ? 'Autorizada' : 'Inativa'}</small></div>)}
                      {!detail.authorizedRecipients.length && <div className="customerEmpty">Nenhum recebedor autorizado.</div>}
                    </div>
                  </section>

                  <section className="customerSummaryStats"><span><small>Pedidos</small><strong>{detail.totalOrders}</strong></span><span><small>Em andamento</small><strong>{detail.activeDeliveries}</strong></span><span><small>Ocorrências</small><strong>{detail.occurrences}</strong></span><span><small>Valor total</small><strong>{formatMoney(detail.totalSpent)}</strong></span></section>
                </div>
              )}

              {tab === 'addresses' && <div className="customerTabContent"><div className="sectionTitle"><strong>Endereços cadastrados</strong><button onClick={() => setFormMode('address')}><Plus size={14}/> Adicionar</button></div><div className="addressList">{detail.addresses.map(address => <article key={address.id}><MapPin size={17}/><div><strong>{address.label} {address.primaryAddress && <em>Principal</em>}</strong><span>{address.street}, {address.number}{address.complement ? ` • ${address.complement}` : ''}</span><small>{address.district ? `${address.district} • ` : ''}{address.city}/{address.state}</small></div></article>)}</div></div>}

              {tab === 'history' && <div className="customerTabContent"><div className="sectionTitle"><strong>Histórico de pedidos</strong></div><CustomerOrders orders={detail.orders} /></div>}

              {tab === 'preferences' && (
                <div className="customerTabContent">
                  <div className="sectionTitle">
                    <strong>Preferências de entrega</strong>
                    <button onClick={() => setPreferenceOpen(true)}><Pencil size={14}/> Editar</button>
                  </div>
                  <div className="preferenceGrid">
                    <span><small>Notificações</small><strong>{detail.preference.notificationsEnabled ? 'Ativadas' : 'Desativadas'}</strong></span>
                    <span><small>Canal</small><strong>{detail.preference.notificationChannel}</strong></span>
                    <span><small>Janela preferencial</small><strong>{detail.preference.preferredStartTime && detail.preference.preferredEndTime ? `${detail.preference.preferredStartTime.slice(0,5)} — ${detail.preference.preferredEndTime.slice(0,5)}` : 'Sem restrição'}</strong></span>
                    <span className="wide"><small>Instruções</small><strong>{detail.preference.deliveryInstructions ?? 'Nenhuma instrução especial.'}</strong></span>
                  </div>
                  <div className="customerPreferenceNote">A janela preferencial ficará disponível para a próxima etapa de integração com o planejamento de Pedidos e o Route Engine.</div>
                </div>
              )}

              {tab === 'occurrences' && <div className="customerTabContent"><div className="sectionTitle"><strong>Ocorrências logísticas</strong></div>{occurrenceOrders.length ? <CustomerOrders orders={occurrenceOrders} /> : <div className="customerEmpty success">Nenhuma ocorrência registrada para este cliente.</div>}</div>}
            </>
          ) : <div className="customerDrawerEmpty"><UsersRound size={28}/><strong>Selecione um cliente</strong><span>Os detalhes serão exibidos aqui.</span></div>}
        </aside>
      </section>

      {formMode && <CustomerFormModal mode={formMode} customerId={selectedId} onClose={() => setFormMode(null)} onSaved={handleSaved} />}
      {preferenceOpen && detail && <PreferenceFormModal customerId={detail.id} preference={detail.preference} onClose={() => setPreferenceOpen(false)} onSaved={handlePreferenceSaved} />}
    </div>
  )
}
