package com.jucelio.rotacerta.domain.model.delivery

/**
 * Informações operacionais retornadas pelo backend para o entregador.
 *
 * O foco desta primeira integração é informar se o cliente autorizou
 * outra pessoa a receber a encomenda e quais instruções devem ser
 * observadas antes de concluir a entrega.
 */
data class DriverDeliveryInfo(
    val orderId: Long,
    val orderNumber: String,
    val trackingCode: String,
    val customerName: String,
    val status: String,
    val alternateRecipientAuthorized: Boolean,
    val alternateRecipientName: String? = null,
    val alternateRecipientRelationship: String? = null,
    val deliveryInstructions: String? = null,
    val recipientUpdatedAt: String? = null
)
