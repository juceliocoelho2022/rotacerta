package com.rotacerta.api.dto;

import java.time.LocalTime;

public record DeliveryPreferenceResponse(
        boolean notificationsEnabled,
        String notificationChannel,
        LocalTime preferredStartTime,
        LocalTime preferredEndTime,
        String deliveryInstructions
) {}
