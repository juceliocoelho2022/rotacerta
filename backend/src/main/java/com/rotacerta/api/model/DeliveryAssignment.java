package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal score;

    @Column(name = "eta_minutes", nullable = false)
    private int etaMinutes;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "sequence_position", nullable = false)
    private int sequencePosition;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    protected DeliveryAssignment() {}

    public DeliveryAssignment(
            OrderEntity order,
            Driver driver,
            BigDecimal distanceKm,
            BigDecimal score,
            int etaMinutes
    ) {
        this.order = order;
        this.driver = driver;
        this.distanceKm = distanceKm;
        this.score = score;
        this.etaMinutes = etaMinutes;
        this.status = "ASSIGNED";
        this.sequencePosition = 999;
        this.assignedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public Driver getDriver() { return driver; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public BigDecimal getScore() { return score; }
    public int getEtaMinutes() { return etaMinutes; }
    public String getStatus() { return status; }
    public int getSequencePosition() { return sequencePosition; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }

    public void setSequencePosition(int sequencePosition) {
        this.sequencePosition = sequencePosition;
    }
}
