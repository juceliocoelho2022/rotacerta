package com.rotacerta.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record OrderDeliveryDetailsResponse(
        Long customerAddressId,
        String addressLabel,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state,
        String zipCode,
        Double latitude,
        Double longitude,
        LocalDate deliveryDate,
        LocalTime windowStart,
        LocalTime windowEnd,
        String instructions
) {}
