package com.rotacerta.api.dto;

import java.util.List;

public record OperationsMonitoringResponse(
        long totalOrders,
        long inProgress,
        long delivered,
        long delayed,
        long activeDrivers,
        double successRate,
        List<MonitoringDriverResponse> drivers,
        List<MonitoringOrderResponse> orders
) {}
