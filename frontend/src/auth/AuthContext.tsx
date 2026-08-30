import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../services/api'
import { clearSession, readSession, saveSession, type AuthSession, type AuthUser } from './session'

type AuthContextValue = {
  user: AuthUser | null
  session: AuthSession | null
  loading: boolean
  login: (email: string, password: string) => Promise<AuthUser>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readSession())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const sync = () => setSession(readSession())
    window.addEventListener('rotacerta:session-changed', sync)
    window.addEventListener('storage', sync)
    return () => {
      window.removeEventListener('rotacerta:session-changed', sync)
      window.removeEventListener('storage', sync)
    }
  }, [])

  useEffect(() => {
    const validate = async () => {
      const current = readSession()
      if (!current) {
        setLoading(false)
        return
      }
      try {
        const { data } = await api.get<AuthUser>('/api/auth/me')
        const validated = { ...current, user: data }
        saveSession(validated)
        setSession(validated)
      } catch {
        clearSession()
        setSession(null)
      } finally {
        setLoading(false)
      }
    }
    void validate()
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    user: session?.user ?? null,
    session,
    loading,
    login: async (email, password) => {
      const { data } = await api.post<AuthSession>('/api/auth/login', { email, password })
      saveSession(data)
      setSession(data)
      return data.user
    },
    logout: async () => {
      const current = readSession()
      try {
        if (current?.refreshToken) {
          await api.post('/api/auth/logout', { refreshToken: current.refreshToken })
        }
      } finally {
        clearSession()
        setSession(null)
      }
    }
  }), [loading, session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth deve ser usado dentro de AuthProvider.')
  return context
}
