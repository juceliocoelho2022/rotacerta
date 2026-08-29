package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneStatus;
import java.math.BigDecimal;

public record DroneResponse(
        Long id,
        String code,
        String model,
        DroneStatus status,
        double latitude,
        double longitude,
        int batteryPercent,
        BigDecimal maxPayloadKg,
        BigDecimal maxRangeKm,
        boolean available
) {}
