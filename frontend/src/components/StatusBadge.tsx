import type { DeliveryStatus } from '../services/api'

const labels: Record<DeliveryStatus, string> = {
  ORDER_CREATED: 'Pedido criado',
  PAYMENT_APPROVED: 'Pagamento aprovado',
  PICKING: 'Em separação',
  PACKING: 'Embalagem',
  READY_FOR_SHIPMENT: 'Pronto para envio',
  SHIPPED: 'Enviado',
  IN_TRANSIT: 'Em transporte',
  OUT_FOR_DELIVERY: 'Saiu para entrega',
  DELIVERED: 'Entregue',
  DELIVERY_FAILED: 'Falha na entrega',
  RETURNED: 'Devolvido',
  CANCELLED: 'Cancelado'
}

export function StatusBadge({ status }: { status: DeliveryStatus }) {
  return <span className={`status status-${status.toLowerCase()}`}>{labels[status]}</span>
}

export { labels as statusLabels }
