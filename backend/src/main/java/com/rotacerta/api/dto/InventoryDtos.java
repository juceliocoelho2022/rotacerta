package com.rotacerta.api.dto;

import com.rotacerta.api.model.InventoryMovementType;
import com.rotacerta.api.model.InventoryReservationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class InventoryDtos {
    private InventoryDtos() {}

    public record InventoryItemResponse(
            Long id,
            Long productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            BigDecimal weightKg,
            BigDecimal volumeM3,
            int totalQuantity,
            int reservedQuantity,
            int availableQuantity,
            int minimumQuantity,
            String warehouseLocation,
            String stockStatus,
            OffsetDateTime updatedAt
    ) {}

    public record ProductCreateRequest(
            @NotBlank @Size(max = 80) String sku,
            @NotBlank @Size(max = 180) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
            @NotNull @DecimalMin("0.000") BigDecimal weightKg,
            @NotNull @DecimalMin("0.0000") BigDecimal volumeM3,
            @Min(0) int initialQuantity,
            @Min(0) int minimumQuantity,
            @Size(max = 120) String warehouseLocation
    ) {}

    public record StockEntryRequest(
            @Min(1) int quantity,
            @Size(max = 300) String reason
    ) {}

    public record InventoryReservationRequest(
            @NotBlank @Size(max = 80) String sku,
            @Min(1) int quantity
    ) {}

    public record ReservationResponse(
            Long reservationId,
            Long orderId,
            String orderNumber,
            String sku,
            String productName,
            int quantity,
            InventoryReservationStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime releasedAt
    ) {}

    public record MovementResponse(
            Long id,
            String sku,
            String productName,
            Long orderId,
            String orderNumber,
            InventoryMovementType movementType,
            int quantity,
            int previousTotal,
            int newTotal,
            int previousReserved,
            int newReserved,
            String reason,
            OffsetDateTime createdAt
    ) {}
}
