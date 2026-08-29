import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles/global.css'
import './styles/operations.css'
import './styles/executive.css'
import './styles/deliveries.css'
import './styles/routes.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)
