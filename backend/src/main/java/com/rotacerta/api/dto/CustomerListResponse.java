package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CustomerListResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean active,
        OffsetDateTime createdAt,
        BigDecimal rating,
        String city,
        String state,
        long totalOrders,
        long activeDeliveries,
        long occurrences,
        OffsetDateTime lastOrderAt,
        BigDecimal totalSpent
) {}
