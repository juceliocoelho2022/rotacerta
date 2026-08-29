package com.rotacerta.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_locations")
public class DeliveryLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private int priority;

    @Column(name = "sla_minutes", nullable = false)
    private int slaMinutes;

    @Column(name = "destination_label", nullable = false, length = 180)
    private String destinationLabel;

    @Column(nullable = false, length = 80)
    private String region;

    protected DeliveryLocation() {}

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getPriority() { return priority; }
    public int getSlaMinutes() { return slaMinutes; }
    public String getDestinationLabel() { return destinationLabel; }
    public String getRegion() { return region; }
}
