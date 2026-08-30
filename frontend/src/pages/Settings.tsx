import { useEffect, useMemo, useState } from 'react'
import { RefreshCw, Save, Settings2, ShieldCheck } from 'lucide-react'
import { api } from '../services/api'

type Setting = {
  id: number
  key: string
  category: string
  label: string
  value: string
  valueType: string
  description: string | null
  updatedAt: string
}

export function Settings() {
  const [settings, setSettings] = useState<Setting[]>([])
  const [drafts, setDrafts] = useState<Record<string, string>>({})
  const [dirty, setDirty] = useState<Set<string>>(new Set())
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)

  async function load() {
    try {
      const response = await api.get<Setting[]>('/api/settings')
      setSettings(response.data)
      setDrafts(Object.fromEntries(response.data.map(setting => [setting.key, setting.value])))
      setDirty(new Set())
      setMessage('')
    } catch {
      setMessage('Não foi possível carregar as configurações.')
    }
  }

  useEffect(() => { void load() }, [])

  const grouped = useMemo(() => settings.reduce<Record<string, Setting[]>>((acc, setting) => {
    ;(acc[setting.category] ??= []).push(setting)
    return acc
  }, {}), [settings])

  function changeValue(key: string, value: string) {
    setDrafts(current => ({ ...current, [key]: value }))
    setDirty(current => new Set(current).add(key))
  }

  async function saveAll() {
    if (!dirty.size) {
      setMessage('Nenhuma alteração pendente.')
      return
    }
    setSaving(true)
    try {
      await Promise.all([...dirty].map(key => api.put(`/api/settings/${encodeURIComponent(key)}`, { value: drafts[key] })))
      await load()
      setMessage('Configurações salvas com sucesso.')
    } catch (error: any) {
      setMessage(error?.response?.data?.detail ?? 'Não foi possível salvar todas as configurações.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="opsSuitePage">
      <header className="opsSuiteHeader">
        <div><span className="opsSuiteEyebrow">GOVERNANÇA DA PLATAFORMA</span><h1>Configurações</h1><p>Parâmetros operacionais persistidos no backend e aplicáveis à evolução da plataforma.</p></div>
        <div className="opsSuiteHeaderActions"><button className="opsButton secondary" onClick={() => void load()}><RefreshCw size={16} /> Recarregar</button><button className="opsButton primary" disabled={saving} onClick={() => void saveAll()}><Save size={16} /> {saving ? 'Salvando...' : `Salvar alterações${dirty.size ? ` (${dirty.size})` : ''}`}</button></div>
      </header>

      <div className="opsSettingsNotice"><ShieldCheck size={20} /><div><strong>Configuração controlada</strong><p>Os valores desta tela são persistidos em PostgreSQL. Integrações externas continuam condicionadas às implementações específicas de cada módulo.</p></div></div>
      {message && <div className="opsMessage">{message}</div>}

      <div className="opsSettingsGrid">
        {Object.entries(grouped).map(([category, items]) => <article className="opsSettingsPanel" key={category}>
          <div className="opsPanelHeading"><div><span className="opsSuiteEyebrow">PARÂMETROS</span><h2><Settings2 size={18} /> {category}</h2></div><span>{items.length}</span></div>
          <div className="opsSettingsList">
            {items.map(setting => {
              const value = drafts[setting.key] ?? setting.value
              const changed = dirty.has(setting.key)
              return <div className={`opsSettingRow ${changed ? 'changed' : ''}`} key={setting.key}>
                <div className="opsSettingCopy"><strong>{setting.label}</strong><p>{setting.description}</p><code>{setting.key}</code></div>
                <div className="opsSettingControl">
                  {setting.valueType === 'BOOLEAN' ? <label className="opsSwitch"><input type="checkbox" checked={value === 'true'} onChange={e => changeValue(setting.key, String(e.target.checked))} /><span /></label> :
                    setting.valueType === 'INTEGER' || setting.valueType === 'DECIMAL' ? <input type="number" value={value} onChange={e => changeValue(setting.key, e.target.value)} /> :
                      setting.key === 'notifications.default_channel' ? <select value={value} onChange={e => changeValue(setting.key, e.target.value)}><option>EMAIL</option><option>SMS</option><option>WHATSAPP</option></select> : <input value={value} onChange={e => changeValue(setting.key, e.target.value)} />}
                  {changed && <small>alterado</small>}
                </div>
              </div>
            })}
          </div>
        </article>)}
      </div>
    </section>
  )
}
