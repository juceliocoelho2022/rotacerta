package com.rotacerta.api.dto;

public record MonitoringDriverResponse(
        Long id,
        String name,
        double latitude,
        double longitude,
        boolean available,
        int currentLoad,
        int maxCapacity,
        String vehiclePlate,
        String vehicleModel,
        String photoUrl
) {}
