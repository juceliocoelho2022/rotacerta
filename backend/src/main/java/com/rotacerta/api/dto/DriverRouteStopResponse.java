package com.rotacerta.api.dto;

import java.math.BigDecimal;

public record DriverRouteStopResponse(
        int position,
        Long orderId,
        String orderNumber,
        double latitude,
        double longitude,
        int priority,
        BigDecimal distanceFromPreviousKm,
        int etaFromNowMinutes
) {}
