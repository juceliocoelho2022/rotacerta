package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import java.time.OffsetDateTime;
import java.util.List;

public record LiveTrackingResponse(
        String trackingCode,
        String orderNumber,
        String customerName,
        DeliveryStatus status,
        OffsetDateTime expiresAt,
        String alternateRecipientName,
        String alternateRecipientRelationship,
        String deliveryInstructions,
        List<TrackingEventResponse> events
) {}
