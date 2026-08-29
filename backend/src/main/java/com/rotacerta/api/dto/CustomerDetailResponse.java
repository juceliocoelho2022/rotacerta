package com.rotacerta.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CustomerDetailResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean active,
        OffsetDateTime createdAt,
        BigDecimal rating,
        long totalOrders,
        long activeDeliveries,
        long occurrences,
        BigDecimal totalSpent,
        List<CustomerAddressResponse> addresses,
        List<AuthorizedRecipientResponse> authorizedRecipients,
        DeliveryPreferenceResponse preference,
        List<CustomerOrderResponse> orders
) {}
