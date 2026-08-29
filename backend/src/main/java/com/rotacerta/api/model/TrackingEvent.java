package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tracking_events")
public class TrackingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    private String location;

    @Column(name = "event_time", nullable = false)
    private OffsetDateTime eventTime;

    protected TrackingEvent() {}

    public TrackingEvent(OrderEntity order, DeliveryStatus status, String location, OffsetDateTime eventTime) {
        this.order = order;
        this.status = status;
        this.location = location;
        this.eventTime = eventTime;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public DeliveryStatus getStatus() { return status; }
    public String getLocation() { return location; }
    public OffsetDateTime getEventTime() { return eventTime; }
}
