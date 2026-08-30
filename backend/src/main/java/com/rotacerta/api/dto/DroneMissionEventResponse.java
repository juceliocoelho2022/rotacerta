package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneMissionStatus;

import java.time.OffsetDateTime;

public record DroneMissionEventResponse(
        Long id,
        String eventType,
        DroneMissionStatus missionStatus,
        String title,
        String description,
        String actor,
        OffsetDateTime createdAt
) {}
