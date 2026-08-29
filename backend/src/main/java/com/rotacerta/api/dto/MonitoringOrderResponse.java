package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;

import java.math.BigDecimal;

public record MonitoringOrderResponse(
        Long id,
        String orderNumber,
        String customerName,
        DeliveryStatus status,
        double latitude,
        double longitude,
        String destinationLabel,
        String region,
        int priority,
        int slaMinutes,
        Long driverId,
        String driverName,
        Integer etaMinutes,
        BigDecimal distanceKm,
        BigDecimal score,
        int riskPercent,
        String riskLevel,
        String riskReason
) {}
