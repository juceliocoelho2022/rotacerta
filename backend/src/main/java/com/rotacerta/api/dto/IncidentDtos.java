package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class IncidentDtos {
    private IncidentDtos() {}

    public record CreateRequest(
            Long orderId,
            Long driverId,
            Long vehicleId,
            @NotBlank String severity,
            @NotBlank String category,
            @NotBlank @Size(max = 140) String title,
            @NotBlank String description,
            @Size(max = 180) String location
    ) {}

    public record StatusUpdateRequest(
            @NotBlank String status,
            String resolution
    ) {}

    public record Response(
            Long id,
            Long orderId,
            String orderNumber,
            Long driverId,
            String driverName,
            Long vehicleId,
            String vehiclePlate,
            String severity,
            String status,
            String category,
            String title,
            String description,
            String location,
            String resolution,
            OffsetDateTime openedAt,
            OffsetDateTime resolvedAt
    ) {}
}
