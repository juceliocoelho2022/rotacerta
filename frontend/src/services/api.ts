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

export type OrderPriority = 'NORMAL' | 'HIGH' | 'URGENT'
export type DeliveryType = 'STANDARD' | 'EXPRESS' | 'SAME_DAY' | 'SCHEDULED'

export interface Order {
  id: number
  orderNumber: string
  customerName: string
  total: number
  status: DeliveryStatus
  priority: OrderPriority
  deliveryType: DeliveryType
  trackingCode: string
  createdAt: string
}

export interface OrderItemDetail {
  id: number
  productName: string
  quantity: number
  unitPrice: number
  weightKg: number
  volumeM3: number
  lineTotal: number
}

export interface OrderDeliveryDetail {
  customerAddressId: number | null
  addressLabel: string
  street: string
  number: string
  complement: string | null
  district: string | null
  city: string
  state: string
  zipCode: string | null
  latitude: number | null
  longitude: number | null
  deliveryDate: string
  windowStart: string | null
  windowEnd: string | null
  instructions: string | null
}

export interface OrderDetail extends Order {
  customerId: number
  totalWeightKg: number
  totalVolumeM3: number
  totalPackages: number
  delivery: OrderDeliveryDetail | null
  items: OrderItemDetail[]
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
  photoUrl: string | null
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

export interface DispatchReadiness {
  orderId: number
  status: DeliveryStatus
  priority: OrderPriority
  deliveryType: DeliveryType
  dispatchableStatus: boolean
  hasCoordinates: boolean
  assigned: boolean
  driverId: number | null
  driverName: string | null
  etaMinutes: number | null
  message: string
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

export interface CustomerListItem {
  id: number
  name: string
  email: string
  phone: string | null
  active: boolean
  createdAt: string
  rating: number
  city: string | null
  state: string | null
  totalOrders: number
  activeDeliveries: number
  occurrences: number
  lastOrderAt: string | null
  totalSpent: number
}

export interface CustomerAddress {
  id: number
  label: string
  street: string
  number: string
  complement: string | null
  district: string | null
  city: string
  state: string
  zipCode: string | null
  latitude: number | null
  longitude: number | null
  primaryAddress: boolean
}

export interface AuthorizedRecipient {
  id: number
  name: string
  relationship: string
  phone: string | null
  active: boolean
}

export interface DeliveryPreference {
  notificationsEnabled: boolean
  notificationChannel: string
  preferredStartTime: string | null
  preferredEndTime: string | null
  deliveryInstructions: string | null
}

export interface CustomerOrder {
  id: number
  orderNumber: string
  status: DeliveryStatus
  trackingCode: string
  total: number
  createdAt: string
}

export interface CustomerDetail {
  id: number
  name: string
  email: string
  phone: string | null
  active: boolean
  createdAt: string
  rating: number
  totalOrders: number
  activeDeliveries: number
  occurrences: number
  totalSpent: number
  addresses: CustomerAddress[]
  authorizedRecipients: AuthorizedRecipient[]
  preference: DeliveryPreference
  orders: CustomerOrder[]
}

export type DroneStatus = 'AVAILABLE' | 'RESERVED' | 'IN_FLIGHT' | 'RETURNING' | 'CHARGING' | 'MAINTENANCE' | 'OFFLINE'
export type DroneMissionStatus = 'PLANNED' | 'AUTHORIZED' | 'LOADING' | 'READY_FOR_TAKEOFF' | 'IN_FLIGHT' | 'APPROACHING' | 'LOWERING_PACKAGE' | 'DELIVERED' | 'RETURNING' | 'COMPLETED' | 'ABORTED'

export interface Drone {
  id: number
  code: string
  model: string
  status: DroneStatus
  latitude: number
  longitude: number
  batteryPercent: number
  maxPayloadKg: number
  maxRangeKm: number
  available: boolean
}

export interface DroneEligibility {
  orderId: number
  orderNumber: string
  eligible: boolean
  payloadKg: number
  destinationLatitude: number
  destinationLongitude: number
  recommendedDroneId: number | null
  recommendedDroneCode: string | null
  estimatedDistanceKm: number | null
  estimatedEtaMinutes: number | null
  blockers: string[]
  pendingExternalChecks: string[]
  mode: string
}

export interface DroneMission {
  id: number
  orderId: number
  orderNumber: string
  droneId: number
  droneCode: string
  droneModel: string
  status: DroneMissionStatus
  payloadKg: number
  distanceKm: number
  etaMinutes: number
  originLatitude: number
  originLongitude: number
  destinationLatitude: number
  destinationLongitude: number
  createdAt: string
  updatedAt: string
}
