package com.rotacerta.api.dto;

import java.math.BigDecimal;

public record RouteOptimizationResponse(
        Long driverId,
        String driverName,
        BigDecimal currentDistanceKm,
        BigDecimal optimizedDistanceKm,
        BigDecimal savedDistanceKm,
        int currentEstimatedMinutes,
        int optimizedEstimatedMinutes,
        int savedMinutes,
        DriverRouteResponse currentRoute,
        DriverRouteResponse optimizedRoute
) {}
