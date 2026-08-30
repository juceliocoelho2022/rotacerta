import { useEffect, useMemo, useState } from 'react'
import { Check, ChevronLeft, ChevronRight, PackagePlus, Plus, Trash2, X } from 'lucide-react'
import {
  api,
  type CustomerDetail,
  type CustomerListItem,
  type DeliveryType,
  type OrderDetail,
  type OrderPriority
} from '../services/api'

type Props = {
  onClose: () => void
  onCreated: (orderId: number) => Promise<void>
}

type DraftItem = {
  sku: string
  productName: string
  quantity: number
  unitPrice: string
  weightKg: string
  volumeM3: string
}

type InventoryProduct = {
  productId: number
  sku: string
  productName: string
  unitPrice: number
  totalQuantity: number
  reservedQuantity: number
  availableQuantity: number
  weightKg?: number
  volumeM3?: number
  stockStatus: 'OK' | 'LOW_STOCK' | 'OUT_OF_STOCK'
}

const steps = ['Cliente', 'Produtos', 'Endereço', 'Entrega', 'Revisão']

function emptyItem(): DraftItem {
  return { sku: '', productName: '', quantity: 1, unitPrice: '', weightKg: '0', volumeM3: '0' }
}

function localDateValue(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function money(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

export function OrderCreateModal({ onClose, onCreated }: Props) {
  const [step, setStep] = useState(0)
  const [customers, setCustomers] = useState<CustomerListItem[]>([])
  const [catalog, setCatalog] = useState<InventoryProduct[]>([])
  const [customerId, setCustomerId] = useState<number | null>(null)
  const [customerDetail, setCustomerDetail] = useState<CustomerDetail | null>(null)
  const [addressId, setAddressId] = useState<number | null>(null)
  const [items, setItems] = useState<DraftItem[]>([emptyItem()])
  const [priority, setPriority] = useState<OrderPriority>('NORMAL')
  const [deliveryType, setDeliveryType] = useState<DeliveryType>('STANDARD')
  const [deliveryDate, setDeliveryDate] = useState(localDateValue())
  const [windowStart, setWindowStart] = useState('')
  const [windowEnd, setWindowEnd] = useState('')
  const [loading, setLoading] = useState(true)
  const [loadingCustomer, setLoadingCustomer] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      api.get<CustomerListItem[]>('/api/customers'),
      api.get<InventoryProduct[]>('/api/inventory')
    ])
      .then(([customersResponse, inventoryResponse]) => {
        setCustomers(customersResponse.data.filter(customer => customer.active))
        setCatalog(inventoryResponse.data)
      })
      .catch(() => setError('Não foi possível carregar clientes e catálogo de estoque.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!customerId) {
      setCustomerDetail(null)
      setAddressId(null)
      return
    }

    setLoadingCustomer(true)
    setError('')
    api.get<CustomerDetail>(`/api/customers/${customerId}`)
      .then(response => {
        const detail = response.data
        setCustomerDetail(detail)
        const primary = detail.addresses.find(address => address.primaryAddress) ?? detail.addresses[0] ?? null
        setAddressId(primary?.id ?? null)
        setWindowStart(detail.preference.preferredStartTime?.slice(0, 5) ?? '')
        setWindowEnd(detail.preference.preferredEndTime?.slice(0, 5) ?? '')
      })
      .catch(() => setError('Não foi possível carregar endereços e preferências do cliente.'))
      .finally(() => setLoadingCustomer(false))
  }, [customerId])

  useEffect(() => {
    if (deliveryType === 'SAME_DAY') setDeliveryDate(localDateValue())
  }, [deliveryType])

  const selectedCustomer = customers.find(customer => customer.id === customerId) ?? null
  const selectedAddress = customerDetail?.addresses.find(address => address.id === addressId) ?? null

  const totals = useMemo(() => {
    return items.reduce((acc, item) => {
      const quantity = Number(item.quantity) || 0
      const unitPrice = Number(item.unitPrice) || 0
      const weight = Number(item.weightKg) || 0
      const volume = Number(item.volumeM3) || 0
      acc.total += quantity * unitPrice
      acc.weight += quantity * weight
      acc.volume += quantity * volume
      acc.packages += quantity
      return acc
    }, { total: 0, weight: 0, volume: 0, packages: 0 })
  }, [items])

  function updateItem(index: number, field: keyof DraftItem, value: string | number) {
    setItems(current => current.map((item, itemIndex) => itemIndex === index ? { ...item, [field]: value } : item))
  }

  function selectSku(index: number, sku: string) {
    const product = catalog.find(item => item.sku === sku)
    if (!product) {
      setItems(current => current.map((item, itemIndex) => itemIndex === index ? emptyItem() : item))
      return
    }

    setItems(current => current.map((item, itemIndex) => itemIndex === index ? {
      ...item,
      sku: product.sku,
      productName: product.productName,
      unitPrice: String(product.unitPrice),
      weightKg: String(product.weightKg ?? 0),
      volumeM3: String(product.volumeM3 ?? 0)
    } : item))
  }

  function addItem() {
    setItems(current => [...current, emptyItem()])
  }

  function removeItem(index: number) {
    setItems(current => current.length === 1 ? current : current.filter((_, itemIndex) => itemIndex !== index))
  }

  function validateCurrentStep() {
    setError('')

    if (step === 0 && !customerId) {
      setError('Selecione o cliente do pedido.')
      return false
    }

    if (step === 1) {
      const invalid = items.some(item => !item.sku || Number(item.quantity) <= 0)
      if (invalid) {
        setError('Selecione um SKU válido do catálogo e informe uma quantidade maior que zero para cada item.')
        return false
      }
    }

    if (step === 2 && !addressId) {
      setError('Selecione um endereço de entrega. Se o cliente ainda não possui endereço, cadastre-o primeiro em Clientes.')
      return false
    }

    if (step === 3) {
      if (!deliveryDate) {
        setError('Informe a data prevista de entrega.')
        return false
      }
      if ((windowStart && !windowEnd) || (!windowStart && windowEnd)) {
        setError('Informe o início e o fim da janela de entrega.')
        return false
      }
      if (windowStart && windowEnd && windowEnd <= windowStart) {
        setError('O fim da janela deve ser posterior ao início.')
        return false
      }
      if (deliveryType === 'SCHEDULED' && (!windowStart || !windowEnd)) {
        setError('Pedidos agendados exigem uma janela de entrega.')
        return false
      }
      if (deliveryType === 'SAME_DAY' && deliveryDate !== localDateValue()) {
        setError('Pedidos Same-Day precisam ser entregues hoje.')
        return false
      }
    }

    return true
  }

  function next() {
    if (validateCurrentStep()) setStep(current => Math.min(4, current + 1))
  }

  async function createOrder() {
    if (!customerId || !addressId || !validateCurrentStep()) return
    setSaving(true)
    setError('')

    try {
      const response = await api.post<OrderDetail>('/api/orders', {
        customerId,
        addressId,
        priority,
        deliveryType,
        deliveryDate,
        windowStart: windowStart || null,
        windowEnd: windowEnd || null,
        items: items.map(item => ({
          sku: item.sku,
          quantity: Number(item.quantity)
        }))
      })
      await onCreated(response.data.id)
    } catch (requestError: any) {
      const message = requestError?.response?.data?.message
        ?? requestError?.response?.data?.detail
        ?? requestError?.response?.data?.error
      setError(typeof message === 'string' ? message : 'Não foi possível criar o pedido. Revise os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="orderModalBackdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}>
      <section className="orderModal" role="dialog" aria-modal="true" aria-labelledby="order-create-title">
        <header className="orderModalHeader">
          <div className="orderModalHeading">
            <span><PackagePlus size={20} /></span>
            <div><h2 id="order-create-title">Novo pedido</h2><p>Crie a solicitação comercial e prepare a execução logística.</p></div>
          </div>
          <button className="orderIconButton" type="button" onClick={onClose} aria-label="Fechar"><X size={18} /></button>
        </header>

        <div className="orderStepper">
          {steps.map((label, index) => (
            <div className={index === step ? 'active' : index < step ? 'done' : ''} key={label}>
              <span>{index < step ? <Check size={14} /> : index + 1}</span>
              <small>{label}</small>
            </div>
          ))}
        </div>

        <div className="orderModalBody">
          {step === 0 && (
            <section className="orderWizardSection">
              <div className="orderWizardTitle"><h3>1. Selecione o cliente</h3><p>Somente clientes ativos podem receber novos pedidos.</p></div>
              {loading ? <div className="orderEmptyState">Carregando clientes e catálogo...</div> : (
                <div className="orderCustomerGrid">
                  {customers.map(customer => (
                    <button className={customer.id === customerId ? 'selected' : ''} type="button" key={customer.id} onClick={() => setCustomerId(customer.id)}>
                      <strong>{customer.name}</strong><span>{customer.email}</span><small>{customer.city ? `${customer.city}/${customer.state}` : 'Sem endereço principal'}</small>
                    </button>
                  ))}
                </div>
              )}
            </section>
          )}

          {step === 1 && (
            <section className="orderWizardSection">
              <div className="orderWizardTitle action"><div><h3>2. Produtos por SKU</h3><p>Os dados comerciais e logísticos vêm do catálogo de estoque; o pedido não aceita preço ou peso digitado manualmente.</p></div><button type="button" onClick={addItem}><Plus size={15}/> Adicionar item</button></div>
              <div className="orderItemsEditor">
                {items.map((item, index) => {
                  const selectedProduct = catalog.find(product => product.sku === item.sku)
                  return (
                    <article key={index}>
                      <div className="orderItemTop"><strong>Item {index + 1}</strong><button type="button" disabled={items.length === 1} onClick={() => removeItem(index)}><Trash2 size={15}/></button></div>
                      <div className="orderFormGrid">
                        <label className="wide">SKU / Produto
                          <select required value={item.sku} onChange={event => selectSku(index, event.target.value)}>
                            <option value="">Selecione um produto do estoque</option>
                            {catalog.map(product => (
                              <option key={product.sku} value={product.sku} disabled={product.availableQuantity <= 0}>
                                {product.sku} — {product.productName} · disponível {product.availableQuantity}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label className="wide">Produto<input readOnly value={item.productName} placeholder="Selecione um SKU" /></label>
                        <label>Quantidade<input type="number" min="1" step="1" max={selectedProduct?.availableQuantity || undefined} value={item.quantity} onChange={event => updateItem(index, 'quantity', Number(event.target.value))} /></label>
                        <label>Preço unitário<input readOnly value={item.unitPrice ? money(Number(item.unitPrice)) : ''} /></label>
                        <label>Peso unitário (kg)<input readOnly value={item.weightKg} /></label>
                        <label>Volume unitário (m³)<input readOnly value={item.volumeM3} /></label>
                      </div>
                    </article>
                  )
                })}
              </div>
              <div className="orderTotalsStrip"><span><small>Itens/volumes</small><strong>{totals.packages}</strong></span><span><small>Peso total</small><strong>{totals.weight.toFixed(3)} kg</strong></span><span><small>Volume total</small><strong>{totals.volume.toFixed(4)} m³</strong></span><span><small>Total</small><strong>{money(totals.total)}</strong></span></div>
            </section>
          )}

          {step === 2 && (
            <section className="orderWizardSection">
              <div className="orderWizardTitle"><h3>3. Endereço de entrega</h3><p>O pedido armazenará um snapshot do endereço escolhido para preservar o histórico.</p></div>
              {loadingCustomer ? <div className="orderEmptyState">Carregando endereços...</div> : customerDetail?.addresses.length ? (
                <div className="orderAddressGrid">
                  {customerDetail.addresses.map(address => (
                    <button className={address.id === addressId ? 'selected' : ''} type="button" key={address.id} onClick={() => setAddressId(address.id)}>
                      <div><strong>{address.label}</strong>{address.primaryAddress && <em>Principal</em>}</div>
                      <span>{address.street}, {address.number}{address.complement ? ` • ${address.complement}` : ''}</span>
                      <small>{address.district ? `${address.district} • ` : ''}{address.city}/{address.state} {address.zipCode ?? ''}</small>
                    </button>
                  ))}
                </div>
              ) : <div className="orderEmptyState warning">Este cliente ainda não possui endereço cadastrado. Cadastre um endereço em Clientes antes de continuar.</div>}
            </section>
          )}

          {step === 3 && (
            <section className="orderWizardSection">
              <div className="orderWizardTitle"><h3>4. Condições da entrega</h3><p>Defina prioridade, modalidade, data e janela de recebimento.</p></div>
              <div className="orderFormGrid">
                <label>Prioridade<select value={priority} onChange={event => setPriority(event.target.value as OrderPriority)}><option value="NORMAL">Normal</option><option value="HIGH">Alta</option><option value="URGENT">Urgente</option></select></label>
                <label>Modalidade<select value={deliveryType} onChange={event => setDeliveryType(event.target.value as DeliveryType)}><option value="STANDARD">Standard</option><option value="EXPRESS">Express</option><option value="SAME_DAY">Same-Day</option><option value="SCHEDULED">Agendada</option></select></label>
                <label>Data de entrega<input type="date" min={localDateValue()} value={deliveryDate} disabled={deliveryType === 'SAME_DAY'} onChange={event => setDeliveryDate(event.target.value)} /></label>
                <label>Início da janela<input type="time" value={windowStart} onChange={event => setWindowStart(event.target.value)} /></label>
                <label>Fim da janela<input type="time" value={windowEnd} onChange={event => setWindowEnd(event.target.value)} /></label>
                <div className="orderPreferenceHint wide"><strong>Preferência do cliente</strong><span>{customerDetail?.preference.preferredStartTime && customerDetail.preference.preferredEndTime ? `${customerDetail.preference.preferredStartTime.slice(0, 5)} — ${customerDetail.preference.preferredEndTime.slice(0, 5)}` : 'Sem janela preferencial'}</span><small>{customerDetail?.preference.deliveryInstructions ?? 'Nenhuma instrução especial cadastrada.'}</small></div>
              </div>
            </section>
          )}

          {step === 4 && (
            <section className="orderWizardSection">
              <div className="orderWizardTitle"><h3>5. Revisão</h3><p>Confira os dados antes de criar o pedido.</p></div>
              <div className="orderReviewGrid">
                <article><small>Cliente</small><strong>{selectedCustomer?.name}</strong><span>{selectedCustomer?.email}</span></article>
                <article><small>Entrega</small><strong>{priority} • {deliveryType}</strong><span>{deliveryDate}{windowStart && windowEnd ? ` • ${windowStart}–${windowEnd}` : ''}</span></article>
                <article className="wide"><small>Destino</small><strong>{selectedAddress ? `${selectedAddress.street}, ${selectedAddress.number}` : '—'}</strong><span>{selectedAddress ? `${selectedAddress.city}/${selectedAddress.state}` : ''}</span></article>
              </div>
              <div className="orderReviewItems">
                {items.map((item, index) => <div key={index}><span>{item.quantity}× {item.productName} <small>({item.sku})</small></span><strong>{money(Number(item.unitPrice) * Number(item.quantity))}</strong></div>)}
              </div>
              <div className="orderTotalsStrip review"><span><small>Volumes</small><strong>{totals.packages}</strong></span><span><small>Peso</small><strong>{totals.weight.toFixed(3)} kg</strong></span><span><small>Volume</small><strong>{totals.volume.toFixed(4)} m³</strong></span><span><small>Total do pedido</small><strong>{money(totals.total)}</strong></span></div>
            </section>
          )}

          {error && <div className="orderFormError">{error}</div>}
        </div>

        <footer className="orderModalActions">
          <button type="button" onClick={step === 0 ? onClose : () => { setError(''); setStep(current => current - 1) }}><ChevronLeft size={16}/>{step === 0 ? 'Cancelar' : 'Voltar'}</button>
          {step < 4 ? <button className="primary" type="button" onClick={next}>Continuar <ChevronRight size={16}/></button> : <button className="primary" type="button" disabled={saving} onClick={createOrder}>{saving ? 'Criando pedido...' : 'Criar pedido'} <Check size={16}/></button>}
        </footer>
      </section>
    </div>
  )
}
