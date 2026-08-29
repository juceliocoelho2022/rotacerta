package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneMissionStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DroneMissionResponse(
        Long id,
        Long orderId,
        String orderNumber,
        Long droneId,
        String droneCode,
        String droneModel,
        DroneMissionStatus status,
        BigDecimal payloadKg,
        BigDecimal distanceKm,
        int etaMinutes,
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
