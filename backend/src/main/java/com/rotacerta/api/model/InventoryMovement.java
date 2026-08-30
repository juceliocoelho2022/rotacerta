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

import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 40)
    private InventoryMovementType movementType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "previous_total", nullable = false)
    private int previousTotal;

    @Column(name = "new_total", nullable = false)
    private int newTotal;

    @Column(name = "previous_reserved", nullable = false)
    private int previousReserved;

    @Column(name = "new_reserved", nullable = false)
    private int newReserved;

    @Column(length = 300)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected InventoryMovement() {}

    public InventoryMovement(Product product, OrderEntity order, InventoryMovementType movementType, int quantity,
                             int previousTotal, int newTotal, int previousReserved, int newReserved, String reason) {
        this.product = product;
        this.order = order;
        this.movementType = movementType;
        this.quantity = quantity;
        this.previousTotal = previousTotal;
        this.newTotal = newTotal;
        this.previousReserved = previousReserved;
        this.newReserved = newReserved;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public OrderEntity getOrder() { return order; }
    public InventoryMovementType getMovementType() { return movementType; }
    public int getQuantity() { return quantity; }
    public int getPreviousTotal() { return previousTotal; }
    public int getNewTotal() { return newTotal; }
    public int getPreviousReserved() { return previousReserved; }
    public int getNewReserved() { return newReserved; }
    public String getReason() { return reason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
