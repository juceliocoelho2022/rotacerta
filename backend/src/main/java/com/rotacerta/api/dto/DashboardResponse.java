package com.rotacerta.api.dto;

public record DashboardResponse(
        long totalOrders,
        long picking,
        long inTransit,
        long outForDelivery,
        long delivered,
        long failed
) {}
