package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "drone_missions")
public class DroneMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id", nullable = false)
    private Drone drone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DroneMissionStatus status;

    @Column(name = "payload_kg", nullable = false, precision = 8, scale = 3)
    private BigDecimal payloadKg;

    @Column(name = "distance_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "eta_minutes", nullable = false)
    private int etaMinutes;

    @Column(name = "origin_latitude", nullable = false)
    private double originLatitude;

    @Column(name = "origin_longitude", nullable = false)
    private double originLongitude;

    @Column(name = "destination_latitude", nullable = false)
    private double destinationLatitude;

    @Column(name = "destination_longitude", nullable = false)
    private double destinationLongitude;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DroneMission() {}

    public DroneMission(OrderEntity order, Drone drone, BigDecimal payloadKg, BigDecimal distanceKm, int etaMinutes,
                        double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) {
        this.order = order;
        this.drone = drone;
        this.status = DroneMissionStatus.PLANNED;
        this.payloadKg = payloadKg;
        this.distanceKm = distanceKm;
        this.etaMinutes = etaMinutes;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public Drone getDrone() { return drone; }
    public DroneMissionStatus getStatus() { return status; }
    public BigDecimal getPayloadKg() { return payloadKg; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public int getEtaMinutes() { return etaMinutes; }
    public double getOriginLatitude() { return originLatitude; }
    public double getOriginLongitude() { return originLongitude; }
    public double getDestinationLatitude() { return destinationLatitude; }
    public double getDestinationLongitude() { return destinationLongitude; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(DroneMissionStatus status) {
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
}
