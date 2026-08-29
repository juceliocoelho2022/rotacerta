import { type FormEvent, useState } from 'react'
import { BellRing, X } from 'lucide-react'
import { api, type DeliveryPreference } from '../services/api'

type Props = {
  customerId: number
  preference: DeliveryPreference
  onClose: () => void
  onSaved: (customerId: number) => Promise<void>
}

export function PreferenceFormModal({ customerId, preference, onClose, onSaved }: Props) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    notificationsEnabled: preference.notificationsEnabled,
    notificationChannel: preference.notificationChannel || 'EMAIL',
    preferredStartTime: preference.preferredStartTime?.slice(0, 5) ?? '',
    preferredEndTime: preference.preferredEndTime?.slice(0, 5) ?? '',
    deliveryInstructions: preference.deliveryInstructions ?? ''
  })

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    const hasStart = Boolean(form.preferredStartTime)
    const hasEnd = Boolean(form.preferredEndTime)

    if (hasStart !== hasEnd) {
      setError('Informe o início e o fim da janela de recebimento, ou deixe ambos vazios.')
      return
    }

    if (hasStart && form.preferredStartTime >= form.preferredEndTime) {
      setError('O horário inicial deve ser anterior ao horário final.')
      return
    }

    setSaving(true)
    try {
      await api.put(`/api/customers/${customerId}/preferences`, {
        notificationsEnabled: form.notificationsEnabled,
        notificationChannel: form.notificationChannel,
        preferredStartTime: form.preferredStartTime || null,
        preferredEndTime: form.preferredEndTime || null,
        deliveryInstructions: form.deliveryInstructions.trim() || null
      })
      await onSaved(customerId)
    } catch (requestError: any) {
      const backendMessage = requestError?.response?.data?.message
        ?? requestError?.response?.data?.detail
        ?? requestError?.response?.data?.error
      setError(typeof backendMessage === 'string' ? backendMessage : 'Não foi possível atualizar as preferências.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="customerModalBackdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}>
      <section className="customerModal" role="dialog" aria-modal="true" aria-labelledby="preference-form-title">
        <header>
          <div className="customerModalTitle">
            <span><BellRing size={20} /></span>
            <div>
              <h2 id="preference-form-title">Preferências de entrega</h2>
              <p>Defina notificações, janela preferencial e instruções para o recebimento.</p>
            </div>
          </div>
          <button className="customerModalClose" type="button" onClick={onClose} aria-label="Fechar"><X size={18} /></button>
        </header>

        <form onSubmit={submit}>
          <div className="customerFormGrid">
            <label className="customerCheck wide">
              <input
                type="checkbox"
                checked={form.notificationsEnabled}
                onChange={event => setForm(current => ({ ...current, notificationsEnabled: event.target.checked }))}
              />
              <span>Receber notificações sobre a entrega</span>
            </label>

            <label>
              Canal preferencial
              <select
                value={form.notificationChannel}
                onChange={event => setForm(current => ({ ...current, notificationChannel: event.target.value }))}
                disabled={!form.notificationsEnabled}
              >
                <option value="EMAIL">E-mail</option>
                <option value="SMS">SMS</option>
                <option value="WHATSAPP">WhatsApp</option>
              </select>
            </label>

            <div className="customerWindowHint">
              <strong>Janela de recebimento</strong>
              <span>O Route Engine poderá usar esta faixa para evitar planejar a entrega fora do horário preferido.</span>
            </div>

            <label>
              Receber a partir de
              <input
                type="time"
                value={form.preferredStartTime}
                onChange={event => setForm(current => ({ ...current, preferredStartTime: event.target.value }))}
              />
            </label>

            <label>
              Receber até
              <input
                type="time"
                value={form.preferredEndTime}
                onChange={event => setForm(current => ({ ...current, preferredEndTime: event.target.value }))}
              />
            </label>

            <label className="wide">
              Instruções de entrega
              <textarea
                maxLength={300}
                rows={4}
                value={form.deliveryInstructions}
                onChange={event => setForm(current => ({ ...current, deliveryInstructions: event.target.value }))}
                placeholder="Ex.: ligar ao chegar, deixar na portaria, interfone 42..."
              />
              <small>{form.deliveryInstructions.length}/300 caracteres</small>
            </label>
          </div>

          {error && <div className="customerFormError">{error}</div>}

          <footer className="customerModalActions">
            <button type="button" onClick={onClose}>Cancelar</button>
            <button className="primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Salvar preferências'}</button>
          </footer>
        </form>
      </section>
    </div>
  )
}
