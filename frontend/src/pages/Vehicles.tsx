import { useEffect, useMemo, useState } from 'react'
import { Fuel, Gauge, Plus, RefreshCw, Search, Truck, UserRound, Wrench } from 'lucide-react'
import { api } from '../services/api'

type Vehicle = {
  id: number
  plate: string
  model: string
  vehicleType: string
  status: string
  maxCapacity: number
  currentOdometerKm: number
  fuelType: string
  nextMaintenanceKm: number | null
  driverId: number | null
  driverName: string | null
  updatedAt: string
}

type Driver = { id: number; name: string }

const statusLabels: Record<string, string> = {
  AVAILABLE: 'Disponível',
  IN_OPERATION: 'Em operação',
  MAINTENANCE: 'Manutenção',
  OUT_OF_SERVICE: 'Fora de serviço'
}

export function Vehicles() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([])
  const [drivers, setDrivers] = useState<Driver[]>([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    plate: '', model: '', vehicleType: 'VAN', maxCapacity: '8', currentOdometerKm: '0',
    fuelType: 'FLEX', nextMaintenanceKm: '10000', driverId: ''
  })

  async function load() {
    setLoading(true)
    try {
      const [vehiclesResponse, driversResponse] = await Promise.all([
        api.get<Vehicle[]>('/api/vehicles'),
        api.get<Driver[]>('/api/drivers')
      ])
      setVehicles(vehiclesResponse.data)
      setDrivers(driversResponse.data)
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar a frota.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void load() }, [])

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    if (!term) return vehicles
    return vehicles.filter(vehicle =>
      [vehicle.plate, vehicle.model, vehicle.vehicleType, vehicle.status, vehicle.driverName ?? '']
        .some(value => value.toLowerCase().includes(term))
    )
  }, [vehicles, search])

  const stats = useMemo(() => ({
    total: vehicles.length,
    available: vehicles.filter(v => v.status === 'AVAILABLE').length,
    operation: vehicles.filter(v => v.status === 'IN_OPERATION').length,
    maintenance: vehicles.filter(v => v.status === 'MAINTENANCE').length
  }), [vehicles])

  async function createVehicle(event: React.FormEvent) {
    event.preventDefault()
    try {
      await api.post('/api/vehicles', {
        ...form,
        maxCapacity: Number(form.maxCapacity),
        currentOdometerKm: Number(form.currentOdometerKm),
        nextMaintenanceKm: form.nextMaintenanceKm ? Number(form.nextMaintenanceKm) : null,
        driverId: form.driverId ? Number(form.driverId) : null
      })
      setForm({ plate: '', model: '', vehicleType: 'VAN', maxCapacity: '8', currentOdometerKm: '0', fuelType: 'FLEX', nextMaintenanceKm: '10000', driverId: '' })
      setShowForm(false)
      setMessage('Veículo cadastrado com sucesso.')
      await load()
    } catch (error: any) {
      setMessage(error?.response?.data?.detail ?? 'Não foi possível cadastrar o veículo.')
    }
  }

  async function updateVehicle(vehicle: Vehicle, status: string, driverId: number | null) {
    try {
      const response = await api.patch<Vehicle>(`/api/vehicles/${vehicle.id}/status`, { status, driverId })
      setVehicles(current => current.map(item => item.id === vehicle.id ? response.data : item))
      setMessage('Frota atualizada.')
    } catch (error: any) {
      setMessage(error?.response?.data?.detail ?? 'Não foi possível atualizar o veículo.')
    }
  }

  return (
    <section className="opsSuitePage">
      <header className="opsSuiteHeader">
        <div>
          <span className="opsSuiteEyebrow">FROTA TERRESTRE</span>
          <h1>Veículos</h1>
          <p>Capacidade, disponibilidade, manutenção e vínculo operacional com motoristas.</p>
        </div>
        <div className="opsSuiteHeaderActions">
          <button className="opsButton secondary" onClick={() => void load()}><RefreshCw size={16} /> Atualizar</button>
          <button className="opsButton primary" onClick={() => setShowForm(value => !value)}><Plus size={16} /> Novo veículo</button>
        </div>
      </header>

      <div className="opsKpiGrid">
        <article className="opsKpi"><Truck /><span>Frota total</span><strong>{stats.total}</strong></article>
        <article className="opsKpi"><Gauge /><span>Disponíveis</span><strong>{stats.available}</strong></article>
        <article className="opsKpi"><UserRound /><span>Em operação</span><strong>{stats.operation}</strong></article>
        <article className="opsKpi"><Wrench /><span>Manutenção</span><strong>{stats.maintenance}</strong></article>
      </div>

      {showForm && (
        <form className="opsFormPanel" onSubmit={createVehicle}>
          <div className="opsFormTitle"><Truck size={18} /><strong>Cadastrar veículo</strong></div>
          <div className="opsFormGrid">
            <label>Placa<input required value={form.plate} onChange={e => setForm({ ...form, plate: e.target.value })} placeholder="ABC1D23" /></label>
            <label>Modelo<input required value={form.model} onChange={e => setForm({ ...form, model: e.target.value })} placeholder="Fiat Fiorino" /></label>
            <label>Tipo<select value={form.vehicleType} onChange={e => setForm({ ...form, vehicleType: e.target.value })}><option>VAN</option><option>TRUCK</option><option>UTILITY</option><option>MOTORCYCLE</option><option>ELECTRIC</option></select></label>
            <label>Capacidade<input required min="1" type="number" value={form.maxCapacity} onChange={e => setForm({ ...form, maxCapacity: e.target.value })} /></label>
            <label>Odômetro (km)<input min="0" step="0.1" type="number" value={form.currentOdometerKm} onChange={e => setForm({ ...form, currentOdometerKm: e.target.value })} /></label>
            <label>Combustível<select value={form.fuelType} onChange={e => setForm({ ...form, fuelType: e.target.value })}><option>FLEX</option><option>DIESEL</option><option>GASOLINE</option><option>ELECTRIC</option><option>HYBRID</option></select></label>
            <label>Próxima manutenção (km)<input min="0" step="0.1" type="number" value={form.nextMaintenanceKm} onChange={e => setForm({ ...form, nextMaintenanceKm: e.target.value })} /></label>
            <label>Motorista<select value={form.driverId} onChange={e => setForm({ ...form, driverId: e.target.value })}><option value="">Sem vínculo</option>{drivers.map(driver => <option key={driver.id} value={driver.id}>{driver.name}</option>)}</select></label>
          </div>
          <div className="opsFormActions"><button type="button" className="opsButton secondary" onClick={() => setShowForm(false)}>Cancelar</button><button className="opsButton primary">Salvar veículo</button></div>
        </form>
      )}

      {message && <div className="opsMessage">{message}</div>}

      <article className="opsTablePanel">
        <div className="opsToolbar">
          <div className="opsSearch"><Search size={16} /><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Buscar placa, modelo, motorista..." /></div>
          <span>{filtered.length} veículo(s)</span>
        </div>
        <div className="opsTableScroll">
          <table className="opsTable">
            <thead><tr><th>Veículo</th><th>Status</th><th>Motorista</th><th>Capacidade</th><th>Odômetro</th><th>Manutenção</th><th>Energia</th></tr></thead>
            <tbody>
              {loading ? <tr><td colSpan={7}>Carregando frota...</td></tr> : filtered.map(vehicle => (
                <tr key={vehicle.id}>
                  <td><strong>{vehicle.plate}</strong><small>{vehicle.model} · {vehicle.vehicleType}</small></td>
                  <td><select className={`opsInlineSelect status-${vehicle.status.toLowerCase()}`} value={vehicle.status} onChange={e => void updateVehicle(vehicle, e.target.value, vehicle.driverId)}>{Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></td>
                  <td><select className="opsInlineSelect" value={vehicle.driverId ?? ''} onChange={e => void updateVehicle(vehicle, vehicle.status, e.target.value ? Number(e.target.value) : null)}><option value="">Sem vínculo</option>{drivers.map(driver => <option key={driver.id} value={driver.id}>{driver.name}</option>)}</select></td>
                  <td>{vehicle.maxCapacity} volumes</td>
                  <td>{Number(vehicle.currentOdometerKm).toLocaleString('pt-BR')} km</td>
                  <td>{vehicle.nextMaintenanceKm == null ? '—' : `${Number(vehicle.nextMaintenanceKm).toLocaleString('pt-BR')} km`}</td>
                  <td><span className="opsMeta"><Fuel size={14} /> {vehicle.fuelType}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  )
}
