import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, Boxes, PackageCheck, RefreshCw, Search, ShieldCheck } from 'lucide-react'
import { api } from '../services/api'
import '../styles/stock.css'

type StockStatus = 'OK' | 'LOW_STOCK' | 'OUT_OF_STOCK'

interface InventoryItem {
  id: number
  productId: number
  sku: string
  productName: string
  unitPrice: number
  totalQuantity: number
  reservedQuantity: number
  availableQuantity: number
  minimumQuantity: number
  warehouseLocation: string | null
  stockStatus: StockStatus
  updatedAt: string
}

type Filter = 'ALL' | StockStatus | 'RESERVED'

export function Stock() {
  const [items, setItems] = useState<InventoryItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState<Filter>('ALL')
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState<InventoryItem | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [reason, setReason] = useState('Reposição de estoque')
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const { data } = await api.get<InventoryItem[]>('/api/inventory')
      setItems(data)
    } catch {
      setError('Não foi possível carregar o estoque.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { void load() }, [])

  const metrics = useMemo(() => ({
    total: items.length,
    healthy: items.filter(item => item.stockStatus === 'OK').length,
    low: items.filter(item => item.stockStatus === 'LOW_STOCK').length,
    out: items.filter(item => item.stockStatus === 'OUT_OF_STOCK').length,
    reserved: items.reduce((sum, item) => sum + item.reservedQuantity, 0)
  }), [items])

  const filtered = useMemo(() => {
    const term = search.trim().toLowerCase()
    return items.filter(item => {
      const matchesFilter = filter === 'ALL'
        || (filter === 'RESERVED' ? item.reservedQuantity > 0 : item.stockStatus === filter)
      const matchesSearch = !term
        || item.sku.toLowerCase().includes(term)
        || item.productName.toLowerCase().includes(term)
        || (item.warehouseLocation ?? '').toLowerCase().includes(term)
      return matchesFilter && matchesSearch
    })
  }, [items, filter, search])

  const addEntry = async () => {
    if (!selected || quantity < 1) return
    setSaving(true)
    setError('')
    try {
      await api.post(`/api/inventory/${encodeURIComponent(selected.sku)}/entries`, { quantity, reason })
      setSelected(null)
      setQuantity(1)
      await load()
    } catch (err: any) {
      setError(err?.response?.data?.message ?? 'Não foi possível registrar a entrada de estoque.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="stockPage">
      <div className="stockHeading">
        <div>
          <span className="eyebrow">Inventory Management</span>
          <h1>Estoque & Reservas</h1>
          <p>Saldo físico, disponibilidade, reservas e nível mínimo por SKU.</p>
        </div>
        <button className="stockRefresh" onClick={() => void load()} disabled={loading}>
          <RefreshCw size={17} /> Atualizar
        </button>
      </div>

      <div className="stockKpis">
        <article><Boxes size={21} /><span>SKUs cadastrados</span><strong>{metrics.total}</strong></article>
        <article><ShieldCheck size={21} /><span>Estoque saudável</span><strong>{metrics.healthy}</strong></article>
        <article><AlertTriangle size={21} /><span>Estoque baixo</span><strong>{metrics.low}</strong></article>
        <article><PackageCheck size={21} /><span>Unidades reservadas</span><strong>{metrics.reserved}</strong></article>
      </div>

      <div className="stockToolbar">
        <div className="stockFilters">
          {([
            ['ALL', 'Todos'], ['OK', 'Disponível'], ['LOW_STOCK', 'Estoque baixo'],
            ['OUT_OF_STOCK', 'Esgotado'], ['RESERVED', 'Com reservas']
          ] as [Filter, string][]).map(([value, label]) => (
            <button key={value} className={filter === value ? 'active' : ''} onClick={() => setFilter(value)}>{label}</button>
          ))}
        </div>
        <label className="stockSearch"><Search size={17} /><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Buscar SKU, produto ou endereço" /></label>
      </div>

      {error && <div className="stockError">{error}</div>}

      <div className="stockTableWrap">
        <table className="stockTable">
          <thead><tr><th>SKU</th><th>Produto</th><th>Local</th><th>Total</th><th>Reservado</th><th>Disponível</th><th>Mínimo</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={9} className="stockEmpty">Carregando estoque...</td></tr>
            ) : filtered.length === 0 ? (
              <tr><td colSpan={9} className="stockEmpty">Nenhum SKU encontrado.</td></tr>
            ) : filtered.map(item => (
              <tr key={item.id}>
                <td><code>{item.sku}</code></td>
                <td><strong>{item.productName}</strong><small>R$ {Number(item.unitPrice).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</small></td>
                <td>{item.warehouseLocation ?? '—'}</td>
                <td>{item.totalQuantity}</td>
                <td>{item.reservedQuantity}</td>
                <td className="availableCell">{item.availableQuantity}</td>
                <td>{item.minimumQuantity}</td>
                <td><span className={`stockBadge ${item.stockStatus.toLowerCase()}`}>{item.stockStatus === 'OK' ? 'OK' : item.stockStatus === 'LOW_STOCK' ? 'BAIXO' : 'ESGOTADO'}</span></td>
                <td><button className="entryButton" onClick={() => setSelected(item)}>+ Entrada</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selected && (
        <div className="stockModalBackdrop" onMouseDown={() => setSelected(null)}>
          <div className="stockModal" onMouseDown={event => event.stopPropagation()}>
            <span className="eyebrow">Movimentação de entrada</span>
            <h2>{selected.productName}</h2>
            <p><code>{selected.sku}</code> · disponível agora: <strong>{selected.availableQuantity}</strong></p>
            <label>Quantidade<input type="number" min={1} value={quantity} onChange={e => setQuantity(Number(e.target.value))} /></label>
            <label>Motivo<input value={reason} onChange={e => setReason(e.target.value)} maxLength={300} /></label>
            <div className="stockModalActions">
              <button className="secondary" onClick={() => setSelected(null)}>Cancelar</button>
              <button className="primary" onClick={() => void addEntry()} disabled={saving || quantity < 1}>{saving ? 'Salvando...' : 'Registrar entrada'}</button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
