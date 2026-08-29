import { type FormEvent, useState } from 'react'
import { MapPin, UserPlus, UsersRound, X } from 'lucide-react'
import { api, type CustomerDetail } from '../services/api'

export type CustomerFormMode = 'customer' | 'address' | 'recipient'

type Props = {
  mode: CustomerFormMode
  customerId: number | null
  onClose: () => void
  onSaved: (customerId: number) => Promise<void>
}

const titles: Record<CustomerFormMode, { title: string; subtitle: string }> = {
  customer: { title: 'Novo cliente', subtitle: 'Cadastre o cliente que fará parte da operação logística.' },
  address: { title: 'Novo endereço', subtitle: 'Adicione um novo local de recebimento para o cliente.' },
  recipient: { title: 'Novo recebedor autorizado', subtitle: 'Cadastre uma pessoa autorizada a receber entregas.' }
}

export function CustomerFormModal({ mode, customerId, onClose, onSaved }: Props) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const [customer, setCustomer] = useState({ name: '', email: '', phone: '' })
  const [address, setAddress] = useState({
    label: 'Casa', street: '', number: '', complement: '', district: '', city: '', state: 'SP', zipCode: '', primaryAddress: false
  })
  const [recipient, setRecipient] = useState({ name: '', relationship: '', phone: '' })

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setError('')

    try {
      if (mode === 'customer') {
        const response = await api.post<CustomerDetail>('/api/customers', {
          name: customer.name,
          email: customer.email,
          phone: customer.phone || null
        })
        await onSaved(response.data.id)
        return
      }

      if (!customerId) {
        setError('Selecione um cliente antes de adicionar este registro.')
        return
      }

      if (mode === 'address') {
        await api.post(`/api/customers/${customerId}/addresses`, {
          label: address.label,
          street: address.street,
          number: address.number,
          complement: address.complement || null,
          district: address.district || null,
          city: address.city,
          state: address.state.toUpperCase(),
          zipCode: address.zipCode || null,
          latitude: null,
          longitude: null,
          primaryAddress: address.primaryAddress
        })
        await onSaved(customerId)
        return
      }

      await api.post(`/api/customers/${customerId}/authorized-recipients`, {
        name: recipient.name,
        relationship: recipient.relationship,
        phone: recipient.phone || null
      })
      await onSaved(customerId)
    } catch (requestError: any) {
      const backendMessage = requestError?.response?.data?.message
        ?? requestError?.response?.data?.detail
        ?? requestError?.response?.data?.error
      setError(typeof backendMessage === 'string' ? backendMessage : 'Não foi possível salvar. Revise os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  const Icon = mode === 'customer' ? UsersRound : mode === 'address' ? MapPin : UserPlus

  return (
    <div className="customerModalBackdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}>
      <section className="customerModal" role="dialog" aria-modal="true" aria-labelledby="customer-form-title">
        <header>
          <div className="customerModalTitle">
            <span><Icon size={20} /></span>
            <div>
              <h2 id="customer-form-title">{titles[mode].title}</h2>
              <p>{titles[mode].subtitle}</p>
            </div>
          </div>
          <button className="customerModalClose" type="button" onClick={onClose} aria-label="Fechar"><X size={18} /></button>
        </header>

        <form onSubmit={submit}>
          {mode === 'customer' && (
            <div className="customerFormGrid">
              <label className="wide">Nome completo<input required maxLength={120} value={customer.name} onChange={event => setCustomer(current => ({ ...current, name: event.target.value }))} placeholder="Ex.: Maria Oliveira" /></label>
              <label>E-mail<input required type="email" maxLength={180} value={customer.email} onChange={event => setCustomer(current => ({ ...current, email: event.target.value }))} placeholder="maria@email.com" /></label>
              <label>Telefone<input maxLength={30} value={customer.phone} onChange={event => setCustomer(current => ({ ...current, phone: event.target.value }))} placeholder="(11) 99999-9999" /></label>
            </div>
          )}

          {mode === 'address' && (
            <div className="customerFormGrid">
              <label>Identificação<input required maxLength={40} value={address.label} onChange={event => setAddress(current => ({ ...current, label: event.target.value }))} placeholder="Casa, Trabalho..." /></label>
              <label>CEP<input maxLength={12} value={address.zipCode} onChange={event => setAddress(current => ({ ...current, zipCode: event.target.value }))} placeholder="00000-000" /></label>
              <label className="wide">Logradouro<input required maxLength={160} value={address.street} onChange={event => setAddress(current => ({ ...current, street: event.target.value }))} placeholder="Rua, avenida..." /></label>
              <label>Número<input required maxLength={30} value={address.number} onChange={event => setAddress(current => ({ ...current, number: event.target.value }))} placeholder="123" /></label>
              <label>Complemento<input maxLength={120} value={address.complement} onChange={event => setAddress(current => ({ ...current, complement: event.target.value }))} placeholder="Apto 32" /></label>
              <label>Bairro<input maxLength={100} value={address.district} onChange={event => setAddress(current => ({ ...current, district: event.target.value }))} placeholder="Jardins" /></label>
              <label>Cidade<input required maxLength={100} value={address.city} onChange={event => setAddress(current => ({ ...current, city: event.target.value }))} placeholder="São Paulo" /></label>
              <label>UF<input required minLength={2} maxLength={2} value={address.state} onChange={event => setAddress(current => ({ ...current, state: event.target.value.toUpperCase() }))} placeholder="SP" /></label>
              <label className="customerCheck wide"><input type="checkbox" checked={address.primaryAddress} onChange={event => setAddress(current => ({ ...current, primaryAddress: event.target.checked }))} /><span>Definir como endereço principal</span></label>
            </div>
          )}

          {mode === 'recipient' && (
            <div className="customerFormGrid">
              <label className="wide">Nome completo<input required maxLength={120} value={recipient.name} onChange={event => setRecipient(current => ({ ...current, name: event.target.value }))} placeholder="Ex.: Ana Oliveira" /></label>
              <label>Relação<input required maxLength={60} value={recipient.relationship} onChange={event => setRecipient(current => ({ ...current, relationship: event.target.value }))} placeholder="Filha, cônjuge, vizinho..." /></label>
              <label>Telefone<input maxLength={30} value={recipient.phone} onChange={event => setRecipient(current => ({ ...current, phone: event.target.value }))} placeholder="(11) 99999-9999" /></label>
            </div>
          )}

          {error && <div className="customerFormError">{error}</div>}

          <footer className="customerModalActions">
            <button type="button" onClick={onClose}>Cancelar</button>
            <button className="primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Salvar'}</button>
          </footer>
        </form>
      </section>
    </div>
  )
}
