package com.rotacerta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAddressCreateRequest(
        @NotBlank @Size(max = 40) String label,
        @NotBlank @Size(max = 160) String street,
        @NotBlank @Size(max = 30) String number,
        @Size(max = 120) String complement,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(min = 2, max = 2) String state,
        @Size(max = 12) String zipCode,
        Double latitude,
        Double longitude,
        boolean primaryAddress
) {}
