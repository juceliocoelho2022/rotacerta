package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.OrderPriority;

public record DispatchReadinessResponse(
        Long orderId,
        DeliveryStatus status,
        OrderPriority priority,
        DeliveryType deliveryType,
        boolean dispatchableStatus,
        boolean hasCoordinates,
        boolean assigned,
        Long driverId,
        String driverName,
        Integer etaMinutes,
        String message
) {}
