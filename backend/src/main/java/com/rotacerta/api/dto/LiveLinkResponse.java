package com.rotacerta.api.dto;

import java.time.OffsetDateTime;

public record LiveLinkResponse(
        String publicUrl,
        OffsetDateTime expiresAt
) {}
