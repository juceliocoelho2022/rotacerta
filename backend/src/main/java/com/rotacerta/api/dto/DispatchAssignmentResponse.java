package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DispatchAssignmentResponse(
        Long assignmentId,
        Long orderId,
        String orderNumber,
        Long driverId,
        String driverName,
        BigDecimal distanceKm,
        BigDecimal score,
        int etaMinutes,
        String status,
        OffsetDateTime assignedAt
) {}
