package com.rotacerta.api.model;

public enum DeliveryStatus {
    ORDER_CREATED,
    PAYMENT_APPROVED,
    PICKING,
    PACKING,
    READY_FOR_SHIPMENT,
    SHIPPED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    DELIVERY_FAILED,
    RETURNED,
    CANCELLED
}
