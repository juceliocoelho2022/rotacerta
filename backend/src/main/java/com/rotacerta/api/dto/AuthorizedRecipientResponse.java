package com.rotacerta.api.dto;

public record AuthorizedRecipientResponse(
        Long id,
        String name,
        String relationship,
        String phone,
        boolean active
) {}
