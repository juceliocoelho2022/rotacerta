package com.rotacerta.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderItemCreateRequest(
        @NotBlank @Size(max = 180) String productName,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        @NotNull @DecimalMin("0.000") BigDecimal weightKg,
        @NotNull @DecimalMin("0.0000") BigDecimal volumeM3
) {}
