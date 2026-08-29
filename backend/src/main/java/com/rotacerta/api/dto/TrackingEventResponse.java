package com.rotacerta.api.dto;

import com.rotacerta.api.model.DeliveryStatus;
import java.time.OffsetDateTime;

public record TrackingEventResponse(
        DeliveryStatus status,
        String location,
        OffsetDateTime eventTime
) {}
