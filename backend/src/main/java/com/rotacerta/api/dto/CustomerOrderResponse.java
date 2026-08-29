package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CustomerOrderResponse(
        Long id,
        String orderNumber,
        DeliveryStatus status,
        String trackingCode,
        BigDecimal total,
        OffsetDateTime createdAt
) {}
