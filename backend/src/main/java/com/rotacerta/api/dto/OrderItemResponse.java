package com.rotacerta.api.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String sku,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal weightKg,
        BigDecimal volumeM3,
        BigDecimal lineTotal
) {}
