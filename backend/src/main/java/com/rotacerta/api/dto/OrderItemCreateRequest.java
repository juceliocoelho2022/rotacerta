package com.rotacerta.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderItemCreateRequest(
        @NotBlank @Size(max = 80) String sku,
        @Min(1) int quantity,
        String productName,
        BigDecimal unitPrice,
        BigDecimal weightKg,
        BigDecimal volumeM3
) {}
