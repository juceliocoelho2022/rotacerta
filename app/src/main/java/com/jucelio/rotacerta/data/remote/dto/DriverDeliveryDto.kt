package com.jucelio.rotacerta.data.remote.dto

import com.jucelio.rotacerta.domain.model.delivery.DriverDeliveryInfo

data class DriverDeliveryDto(
    val orderId: Long,
    val orderNumber: String,
    val trackingCode: String,
    val customerName: String,
    val status: String,
    val alternateRecipientAuthorized: Boolean,
    val alternateRecipientName: String?,
    val alternateRecipientRelationship: String?,
    val deliveryInstructions: String?,
    val recipientUpdatedAt: String?
) {
    fun toDomain(): DriverDeliveryInfo = DriverDeliveryInfo(
        orderId = orderId,
        orderNumber = orderNumber,
        trackingCode = trackingCode,
        customerName = customerName,
        status = status,
        alternateRecipientAuthorized = alternateRecipientAuthorized,
        alternateRecipientName = alternateRecipientName,
        alternateRecipientRelationship = alternateRecipientRelationship,
        deliveryInstructions = deliveryInstructions,
        recipientUpdatedAt = recipientUpdatedAt
    )
}
