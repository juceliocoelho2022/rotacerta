package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private boolean available;

    @Column(name = "current_load", nullable = false)
    private int currentLoad;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "vehicle_plate", nullable = false, length = 20)
    private String vehiclePlate;

    @Column(name = "vehicle_model", nullable = false, length = 80)
    private String vehicleModel;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Driver() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isAvailable() { return available; }
    public int getCurrentLoad() { return currentLoad; }
    public int getMaxCapacity() { return maxCapacity; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getVehicleModel() { return vehicleModel; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public boolean hasCapacity() {
        return available && currentLoad < maxCapacity;
    }

    public double loadRatio() {
        return maxCapacity == 0 ? 1.0 : (double) currentLoad / maxCapacity;
    }

    public void incrementLoad() {
        if (currentLoad >= maxCapacity) {
            throw new IllegalStateException("Motorista sem capacidade disponível.");
        }
        this.currentLoad++;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = OffsetDateTime.now();
    }
}
