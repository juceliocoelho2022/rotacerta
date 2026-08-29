package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.OrderPriority;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNumber,
        Long customerId,
        String customerName,
        BigDecimal total,
        DeliveryStatus status,
        OrderPriority priority,
        DeliveryType deliveryType,
        String trackingCode,
        OffsetDateTime createdAt,
        BigDecimal totalWeightKg,
        BigDecimal totalVolumeM3,
        int totalPackages,
        OrderDeliveryDetailsResponse delivery,
        List<OrderItemResponse> items
) {}
