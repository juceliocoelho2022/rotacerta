import { type FormEvent, useState } from 'react'
import { MapPin, Truck, UserPlus, X } from 'lucide-react'
import { api, type MonitoringDriver } from '../services/api'

type Props = {
  onClose: () => void
  onSaved: (driverId: number) => Promise<void>
}

function parseCoordinate(value: string) {
  const normalized = Number(value.replace(',', '.'))
  return Number.isFinite(normalized) ? normalized : null
}

export function DriverCreateModal({ onClose, onSaved }: Props) {
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    name: '',
    latitude: '',
    longitude: '',
    maxCapacity: '8',
    vehiclePlate: '',
    vehicleModel: '',
    available: true
  })

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    const latitude = parseCoordinate(form.latitude)
    const longitude = parseCoordinate(form.longitude)
    const maxCapacity = Number(form.maxCapacity)

    if (latitude === null || latitude < -90 || latitude > 90) {
      setError('Informe uma latitude válida entre -90 e 90.')
      return
    }

    if (longitude === null || longitude < -180 || longitude > 180) {
      setError('Informe uma longitude válida entre -180 e 180.')
      return
    }

    if (!Number.isInteger(maxCapacity) || maxCapacity < 1) {
      setError('A capacidade máxima deve ser um número inteiro maior que zero.')
      return
    }

    setSaving(true)
    try {
      const response = await api.post<MonitoringDriver>('/api/drivers', {
        name: form.name.trim(),
        latitude,
        longitude,
        available: form.available,
        maxCapacity,
        vehiclePlate: form.vehiclePlate.trim().toUpperCase(),
        vehicleModel: form.vehicleModel.trim()
      })
      await onSaved(response.data.id)
    } catch (requestError: any) {
      const backendMessage = requestError?.response?.data?.message
        ?? requestError?.response?.data?.detail
        ?? requestError?.response?.data?.error
      setError(typeof backendMessage === 'string' ? backendMessage : 'Não foi possível cadastrar o motorista.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="driverModalBackdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}>
      <section className="driverModal" role="dialog" aria-modal="true" aria-labelledby="driver-create-title">
        <header className="driverModalHeader">
          <div className="driverModalHeading">
            <span><UserPlus size={20} /></span>
            <div>
              <h2 id="driver-create-title">Novo motorista</h2>
              <p>Cadastre os dados operacionais usados pelo Smart Dispatch.</p>
            </div>
          </div>
          <button className="driverModalClose" type="button" onClick={onClose} aria-label="Fechar"><X size={18} /></button>
        </header>

        <form onSubmit={submit}>
          <div className="driverModalBody">
            <div className="driverFormSectionTitle"><UserPlus size={16}/><strong>Motorista</strong></div>
            <div className="driverFormGrid">
              <label className="wide">Nome completo<input required maxLength={120} value={form.name} onChange={event => setForm(current => ({ ...current, name: event.target.value }))} placeholder="Ex.: Ricardo Alves" /></label>
            </div>

            <div className="driverFormSectionTitle"><Truck size={16}/><strong>Veículo atual</strong></div>
            <div className="driverFormGrid">
              <label>Placa<input required maxLength={20} value={form.vehiclePlate} onChange={event => setForm(current => ({ ...current, vehiclePlate: event.target.value.toUpperCase() }))} placeholder="ABC1D23" /></label>
              <label>Modelo<input required maxLength={80} value={form.vehicleModel} onChange={event => setForm(current => ({ ...current, vehicleModel: event.target.value }))} placeholder="Fiat Fiorino" /></label>
              <label>Capacidade máxima<input required min="1" max="1000" step="1" type="number" value={form.maxCapacity} onChange={event => setForm(current => ({ ...current, maxCapacity: event.target.value }))} /></label>
              <label className="driverAvailabilityCheck"><input type="checkbox" checked={form.available} onChange={event => setForm(current => ({ ...current, available: event.target.checked }))}/><span>Disponível para novos despachos</span></label>
            </div>

            <div className="driverFormSectionTitle"><MapPin size={16}/><strong>Posição operacional inicial</strong></div>
            <div className="driverFormGrid">
              <label>Latitude<input required inputMode="decimal" value={form.latitude} onChange={event => setForm(current => ({ ...current, latitude: event.target.value }))} placeholder="-23.550520" /></label>
              <label>Longitude<input required inputMode="decimal" value={form.longitude} onChange={event => setForm(current => ({ ...current, longitude: event.target.value }))} placeholder="-46.633308" /></label>
            </div>
            <p className="driverFormHint">As coordenadas são persistidas como posição operacional. Telemetria GPS em tempo real ainda depende de integração externa.</p>

            {error && <div className="driverFormError">{error}</div>}
          </div>

          <footer className="driverModalActions">
            <button type="button" onClick={onClose}>Cancelar</button>
            <button className="primary" type="submit" disabled={saving}>{saving ? 'Salvando...' : 'Cadastrar motorista'}</button>
          </footer>
        </form>
      </section>
    </div>
  )
}
