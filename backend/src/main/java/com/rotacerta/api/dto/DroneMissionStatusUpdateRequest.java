package com.rotacerta.api.dto;

import com.rotacerta.api.model.DroneMissionStatus;
import jakarta.validation.constraints.NotNull;

public record DroneMissionStatusUpdateRequest(
        @NotNull DroneMissionStatus status
) {}
