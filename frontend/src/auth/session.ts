export type UserRole = 'ADMIN' | 'CUSTOMER' | 'DRIVER'

export interface AuthUser {
  id: number
  email: string
  displayName: string
  role: UserRole
  customerId: number | null
  driverId: number | null
}

export interface AuthSession {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresInSeconds: number
  user: AuthUser
}

const SESSION_KEY = 'rotacerta.auth.session'

export function readSession(): AuthSession | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY)
    return raw ? JSON.parse(raw) as AuthSession : null
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export function saveSession(session: AuthSession) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  window.dispatchEvent(new CustomEvent('rotacerta:session-changed'))
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
  window.dispatchEvent(new CustomEvent('rotacerta:session-changed'))
}

export function accessToken() {
  return readSession()?.accessToken ?? null
}

export function refreshToken() {
  return readSession()?.refreshToken ?? null
}
