import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
})

export type DeliveryStatus =
  | 'ORDER_CREATED'
  | 'PAYMENT_APPROVED'
  | 'PICKING'
  | 'PACKING'
  | 'READY_FOR_SHIPMENT'
  | 'SHIPPED'
  | 'IN_TRANSIT'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'DELIVERY_FAILED'
  | 'RETURNED'
  | 'CANCELLED'

export interface Order {
  id: number
  orderNumber: string
  customerName: string
  total: number
  status: DeliveryStatus
  trackingCode: string
  createdAt: string
}

export interface DashboardData {
  totalOrders: number
  picking: number
  inTransit: number
  outForDelivery: number
  delivered: number
  failed: number
}

export interface TrackingEvent {
  status: DeliveryStatus
  location: string | null
  eventTime: string
}

export interface TrackingData {
  trackingCode: string
  orderNumber: string
  customerName: string
  status: DeliveryStatus
  events: TrackingEvent[]
}
