package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record DeliveryPreferenceUpdateRequest(
        boolean notificationsEnabled,
        @NotBlank @Size(max = 30) String notificationChannel,
        LocalTime preferredStartTime,
        LocalTime preferredEndTime,
        @Size(max = 300) String deliveryInstructions
) {}
