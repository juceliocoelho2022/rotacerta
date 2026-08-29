package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.OrderPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OrderCreateRequest(
        @NotNull Long customerId,
        @NotNull Long addressId,
        @NotNull OrderPriority priority,
        @NotNull DeliveryType deliveryType,
        @NotNull @FutureOrPresent LocalDate deliveryDate,
        LocalTime windowStart,
        LocalTime windowEnd,
        @NotEmpty List<@Valid OrderItemCreateRequest> items
) {}
