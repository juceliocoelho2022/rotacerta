package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull DeliveryStatus status,
        String location
) {}
