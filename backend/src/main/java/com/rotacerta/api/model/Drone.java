package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "drones")
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DroneStatus status;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "battery_percent", nullable = false)
    private int batteryPercent;

    @Column(name = "max_payload_kg", nullable = false, precision = 8, scale = 3)
    private BigDecimal maxPayloadKg;

    @Column(name = "max_range_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal maxRangeKm;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Drone() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getModel() { return model; }
    public DroneStatus getStatus() { return status; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getBatteryPercent() { return batteryPercent; }
    public BigDecimal getMaxPayloadKg() { return maxPayloadKg; }
    public BigDecimal getMaxRangeKm() { return maxRangeKm; }
    public boolean isAvailable() { return available; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public boolean operationallyAvailable() {
        return available && status == DroneStatus.AVAILABLE && batteryPercent >= 30;
    }

    public void reserve() {
        this.available = false;
        this.status = DroneStatus.RESERVED;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
        this.available = status == DroneStatus.AVAILABLE;
        this.updatedAt = OffsetDateTime.now();
    }
}
