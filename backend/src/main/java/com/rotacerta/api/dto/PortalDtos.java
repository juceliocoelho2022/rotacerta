package com.rotacerta.api.dto;

public final class PortalDtos {
    private PortalDtos() {}

    public record DriverPortalResponse(
            Long driverId,
            String accountName,
            String email,
            String driverName,
            boolean available,
            int currentLoad,
            int maxCapacity,
            String vehiclePlate,
            String vehicleModel,
            String photoUrl
    ) {}
}
