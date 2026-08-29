package com.rotacerta.api.dto;

public record CustomerAddressResponse(
        Long id,
        String label,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state,
        String zipCode,
        Double latitude,
        Double longitude,
        boolean primaryAddress
) {}
