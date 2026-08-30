package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String plate;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "vehicle_type", nullable = false, length = 30)
    private String vehicleType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_odometer_km", nullable = false, precision = 12, scale = 1)
    private BigDecimal currentOdometerKm;

    @Column(name = "fuel_type", nullable = false, length = 30)
    private String fuelType;

    @Column(name = "next_maintenance_km", precision = 12, scale = 1)
    private BigDecimal nextMaintenanceKm;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Vehicle() {}

    public Vehicle(String plate, String model, String vehicleType, int maxCapacity,
                   BigDecimal currentOdometerKm, String fuelType, BigDecimal nextMaintenanceKm, Long driverId) {
        this.plate = plate.toUpperCase();
        this.model = model;
        this.vehicleType = vehicleType.toUpperCase();
        this.status = "AVAILABLE";
        this.maxCapacity = maxCapacity;
        this.currentOdometerKm = currentOdometerKm == null ? BigDecimal.ZERO : currentOdometerKm;
        this.fuelType = fuelType.toUpperCase();
        this.nextMaintenanceKm = nextMaintenanceKm;
        this.driverId = driverId;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getPlate() { return plate; }
    public String getModel() { return model; }
    public String getVehicleType() { return vehicleType; }
    public String getStatus() { return status; }
    public int getMaxCapacity() { return maxCapacity; }
    public BigDecimal getCurrentOdometerKm() { return currentOdometerKm; }
    public String getFuelType() { return fuelType; }
    public BigDecimal getNextMaintenanceKm() { return nextMaintenanceKm; }
    public Long getDriverId() { return driverId; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateAssignment(Long driverId) {
        this.driverId = driverId;
        this.updatedAt = OffsetDateTime.now();
    }
}
