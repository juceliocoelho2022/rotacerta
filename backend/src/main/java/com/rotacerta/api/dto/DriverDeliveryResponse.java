package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;

import java.time.OffsetDateTime;

public record DriverDeliveryResponse(
        Long orderId,
        String orderNumber,
        String trackingCode,
        String customerName,
        DeliveryStatus status,
        boolean alternateRecipientAuthorized,
        String alternateRecipientName,
        String alternateRecipientRelationship,
        String deliveryInstructions,
        OffsetDateTime recipientUpdatedAt
) {}
