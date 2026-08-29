import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles/global.css'
import './styles/operations.css'
import './styles/executive.css'
import './styles/deliveries.css'
import './styles/routes.css'
import './styles/customers.css'
import './styles/customer-forms.css'
import './styles/orders.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>
)
