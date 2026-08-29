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

export interface MonitoringDriver {
  id: number
  name: string
  latitude: number
  longitude: number
  available: boolean
  currentLoad: number
  maxCapacity: number
  vehiclePlate: string
  vehicleModel: string
}

export interface MonitoringOrder {
  id: number
  orderNumber: string
  customerName: string
  status: DeliveryStatus
  latitude: number
  longitude: number
  destinationLabel: string
  region: string
  priority: number
  slaMinutes: number
  driverId: number | null
  driverName: string | null
  etaMinutes: number | null
  distanceKm: number | null
  score: number | null
  riskPercent: number
  riskLevel: string
  riskReason: string
}

export interface OperationsMonitoring {
  totalOrders: number
  inProgress: number
  delivered: number
  delayed: number
  activeDrivers: number
  successRate: number
  drivers: MonitoringDriver[]
  orders: MonitoringOrder[]
}

export interface DispatchAssignment {
  assignmentId: number
  orderId: number
  orderNumber: string
  driverId: number
  driverName: string
  distanceKm: number
  score: number
  etaMinutes: number
  status: string
  assignedAt: string
}

export interface DriverRouteStop {
  position: number
  orderId: number
  orderNumber: string
  latitude: number
  longitude: number
  priority: number
  distanceFromPreviousKm: number
  etaFromNowMinutes: number
}

export interface DriverRoute {
  driverId: number
  driverName: string
  totalStops: number
  totalDistanceKm: number
  estimatedRouteMinutes: number
  stops: DriverRouteStop[]
}

export interface RouteOptimization {
  driverId: number
  driverName: string
  currentDistanceKm: number
  optimizedDistanceKm: number
  savedDistanceKm: number
  currentEstimatedMinutes: number
  optimizedEstimatedMinutes: number
  savedMinutes: number
  currentRoute: DriverRoute
  optimizedRoute: DriverRoute
}
