package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public final class SettingsDtos {
    private SettingsDtos() {}

    public record UpdateRequest(@NotBlank String value) {}

    public record Response(
            Long id,
            String key,
            String category,
            String label,
            String value,
            String valueType,
            String description,
            OffsetDateTime updatedAt
    ) {}
}
