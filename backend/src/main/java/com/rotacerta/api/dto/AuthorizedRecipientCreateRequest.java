package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorizedRecipientCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 60) String relationship,
        @Size(max = 30) String phone
) {}
