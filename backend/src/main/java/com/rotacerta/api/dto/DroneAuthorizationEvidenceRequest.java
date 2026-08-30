package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DroneAuthorizationEvidenceRequest(
        @NotBlank @Size(max = 80) String evidenceType,
        @NotBlank @Size(max = 300) String reference,
        @Size(max = 1000) String description
) {}
