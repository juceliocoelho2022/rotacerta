package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    public DeliveryLocation(
            OrderEntity order,
            double latitude,
            double longitude,
            int priority,
            int slaMinutes,
            String destinationLabel,
            String region
    ) {
        this.order = order;
        updatePlanning(latitude, longitude, priority, slaMinutes, destinationLabel, region);
    }

    public void updatePlanning(
            double latitude,
            double longitude,
            int priority,
            int slaMinutes,
            String destinationLabel,
            String region
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.priority = Math.max(1, priority);
        this.slaMinutes = Math.max(15, slaMinutes);
        this.destinationLabel = destinationLabel;
        this.region = region;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getPriority() { return priority; }
    public int getSlaMinutes() { return slaMinutes; }
    public String getDestinationLabel() { return destinationLabel; }
    public String getRegion() { return region; }
}
