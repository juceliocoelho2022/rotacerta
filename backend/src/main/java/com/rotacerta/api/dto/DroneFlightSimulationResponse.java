package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneMissionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DroneFlightSimulationResponse(
        Long missionId,
        Long orderId,
        String orderNumber,
        String droneCode,
        DroneMissionStatus status,
        String mode,
        String phase,
        BigDecimal progressPercent,
        BigDecimal legProgressPercent,
        Double currentLatitude,
        Double currentLongitude,
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude,
        BigDecimal totalDistanceKm,
        BigDecimal remainingDistanceKm,
        int etaMinutes,
        int remainingEtaMinutes,
        boolean moving,
        String positionSource,
        OffsetDateTime calculatedAt
) {
}
