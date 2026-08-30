import { useState } from 'react'
import { KeyRound, LockKeyhole, ShieldCheck, Truck, UserRound } from 'lucide-react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const demoAccounts = [
  { label: 'Administrador', email: 'admin@rotacerta.local', password: 'Admin@123', icon: ShieldCheck },
  { label: 'Motorista', email: 'driver@rotacerta.local', password: 'Driver@123', icon: Truck },
  { label: 'Cliente', email: 'customer@rotacerta.local', password: 'Customer@123', icon: UserRound }
]

export function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('admin@rotacerta.local')
  const [password, setPassword] = useState('Admin@123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) {
    return <Navigate to={user.role === 'ADMIN' ? '/' : '/portal'} replace />
  }

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const authenticated = await login(email, password)
      const from = (location.state as { from?: string } | null)?.from
      navigate(authenticated.role === 'ADMIN' ? (from ?? '/') : '/portal', { replace: true })
    } catch (requestError: any) {
      setError(requestError?.response?.data?.message ?? 'Não foi possível autenticar. Verifique e-mail e senha.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="loginPage">
      <section className="loginHero">
        <div className="loginBrand"><span>RC</span><strong>RotaCerta</strong></div>
        <div className="loginPitch">
          <span className="loginEyebrow">Secure Logistics Operations</span>
          <h1>Autenticação e acesso por perfil.</h1>
          <p>JWT, refresh token rotativo e RBAC para separar administração, operação do motorista e experiência do cliente.</p>
          <div className="loginSecurityPoints">
            <span><ShieldCheck size={17}/> Spring Security</span>
            <span><KeyRound size={17}/> JWT Access Token</span>
            <span><LockKeyhole size={17}/> ADMIN · DRIVER · CUSTOMER</span>
          </div>
        </div>
        <small>RotaCerta · Smart Logistics Platform</small>
      </section>

      <section className="loginPanel">
        <div className="loginCard">
          <header>
            <span className="loginLock"><LockKeyhole size={22}/></span>
            <div><h2>Entrar no RotaCerta</h2><p>Use sua conta para acessar o ambiente autorizado.</p></div>
          </header>

          <form onSubmit={submit}>
            <label>E-mail<input type="email" required value={email} onChange={event => setEmail(event.target.value)} autoComplete="username" /></label>
            <label>Senha<input type="password" required value={password} onChange={event => setPassword(event.target.value)} autoComplete="current-password" /></label>
            {error && <div className="loginError">{error}</div>}
            <button type="submit" disabled={loading}>{loading ? 'Autenticando...' : 'Entrar com segurança'}</button>
          </form>

          <div className="demoAccounts">
            <div><strong>Contas de demonstração</strong><small>Ativas somente quando o seed local está habilitado.</small></div>
            {demoAccounts.map(({ label, email: demoEmail, password: demoPassword, icon: Icon }) => (
              <button type="button" key={demoEmail} onClick={() => { setEmail(demoEmail); setPassword(demoPassword); setError('') }}>
                <Icon size={16}/><span>{label}</span><small>{demoEmail}</small>
              </button>
            ))}
          </div>
        </div>
      </section>
    </main>
  )
}
