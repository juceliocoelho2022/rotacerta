package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        String customerName,
        BigDecimal total,
        DeliveryStatus status,
        String trackingCode,
        OffsetDateTime createdAt
) {}
