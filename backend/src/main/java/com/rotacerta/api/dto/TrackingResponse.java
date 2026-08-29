package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import java.util.List;

public record TrackingResponse(
        String trackingCode,
        String orderNumber,
        String customerName,
        DeliveryStatus status,
        List<TrackingEventResponse> events
) {}
