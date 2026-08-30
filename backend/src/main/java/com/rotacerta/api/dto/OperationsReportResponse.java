package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record OperationsReportResponse(
        long totalOrders,
        long deliveredOrders,
        long inProgressOrders,
        long failedOrders,
        double deliverySuccessRate,
        BigDecimal deliveredRevenue,
        long totalDrivers,
        long activeDrivers,
        long totalVehicles,
        long availableVehicles,
        long maintenanceVehicles,
        long totalDrones,
        long availableDrones,
        long openIncidents,
        long criticalIncidents,
        Map<String, Long> ordersByStatus
) {}
