package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 180)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    protected Incident() {}

    public Incident(Long orderId, Long driverId, Long vehicleId, String severity, String category,
                    String title, String description, String location) {
        this.orderId = orderId;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.severity = severity.toUpperCase();
        this.status = "OPEN";
        this.category = category.toUpperCase();
        this.title = title;
        this.description = description;
        this.location = location;
        this.openedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getDriverId() { return driverId; }
    public Long getVehicleId() { return vehicleId; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getResolution() { return resolution; }
    public OffsetDateTime getOpenedAt() { return openedAt; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }

    public void updateStatus(String status, String resolution) {
        this.status = status;
        this.resolution = resolution;
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            this.resolvedAt = OffsetDateTime.now();
        } else {
            this.resolvedAt = null;
        }
    }
}
