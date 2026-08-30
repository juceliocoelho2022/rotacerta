package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneAuthorizationDecision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DroneAuthorizationRequest(
        @NotNull DroneAuthorizationDecision decision,
        @NotBlank @Size(max = 160) String authorizedBy,
        @NotBlank @Size(max = 1000) String reason,
        @Min(5) @Max(240) int validMinutes,
        List<@Valid DroneAuthorizationEvidenceRequest> evidence
) {}
