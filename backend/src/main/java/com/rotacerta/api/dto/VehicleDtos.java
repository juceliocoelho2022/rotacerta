package com.rotacerta.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class VehicleDtos {
    private VehicleDtos() {}

    public record CreateRequest(
            @NotBlank String plate,
            @NotBlank String model,
            @NotBlank String vehicleType,
            @Min(1) int maxCapacity,
            @PositiveOrZero BigDecimal currentOdometerKm,
            @NotBlank String fuelType,
            @PositiveOrZero BigDecimal nextMaintenanceKm,
            Long driverId
    ) {}

    public record StatusUpdateRequest(
            @NotBlank String status,
            Long driverId
    ) {}

    public record Response(
            Long id,
            String plate,
            String model,
            String vehicleType,
            String status,
            int maxCapacity,
            BigDecimal currentOdometerKm,
            String fuelType,
            BigDecimal nextMaintenanceKm,
            Long driverId,
            String driverName,
            OffsetDateTime updatedAt
    ) {}
}
