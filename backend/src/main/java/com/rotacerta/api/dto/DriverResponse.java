package com.rotacerta.api.dto;

import java.time.OffsetDateTime;

public record DriverResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        boolean available,
        int currentLoad,
        int maxCapacity,
        String vehiclePlate,
        String vehicleModel,
        OffsetDateTime updatedAt
) {}
