package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    private DeliveryType deliveryType;

    @Column(name = "tracking_code", nullable = false, unique = true)
    private String trackingCode;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected OrderEntity() {}

    public OrderEntity(
            String orderNumber,
            Customer customer,
            BigDecimal total,
            DeliveryStatus status,
            OrderPriority priority,
            DeliveryType deliveryType,
            String trackingCode
    ) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.total = total;
        this.status = status;
        this.priority = priority;
        this.deliveryType = deliveryType;
        this.trackingCode = trackingCode;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getTotal() { return total; }
    public DeliveryStatus getStatus() { return status; }
    public OrderPriority getPriority() { return priority; }
    public DeliveryType getDeliveryType() { return deliveryType; }
    public String getTrackingCode() { return trackingCode; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
}
