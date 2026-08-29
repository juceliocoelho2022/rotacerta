package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DroneEligibilityResponse(
        Long orderId,
        String orderNumber,
        boolean eligible,
        BigDecimal payloadKg,
        double destinationLatitude,
        double destinationLongitude,
        Long recommendedDroneId,
        String recommendedDroneCode,
        BigDecimal estimatedDistanceKm,
        Integer estimatedEtaMinutes,
        List<String> blockers,
        List<String> pendingExternalChecks,
        String mode
) {}
