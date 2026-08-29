package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.OrderPriority;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        String customerName,
        BigDecimal total,
        DeliveryStatus status,
        OrderPriority priority,
        DeliveryType deliveryType,
        String trackingCode,
        OffsetDateTime createdAt
) {}
