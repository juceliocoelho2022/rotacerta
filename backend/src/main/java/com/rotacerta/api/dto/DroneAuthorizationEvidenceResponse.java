package com.rotacerta.api.dto;

import java.time.OffsetDateTime;

public record DroneAuthorizationEvidenceResponse(
        Long id,
        String evidenceType,
        String reference,
        String description,
        OffsetDateTime createdAt
) {}
