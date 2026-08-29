package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DriverRouteResponse(
        Long driverId,
        String driverName,
        int totalStops,
        BigDecimal totalDistanceKm,
        int estimatedRouteMinutes,
        List<DriverRouteStopResponse> stops
) {}
