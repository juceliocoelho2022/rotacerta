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

import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "minimum_quantity", nullable = false)
    private int minimumQuantity;

    @Column(name = "warehouse_location", length = 120)
    private String warehouseLocation;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected Inventory() {}

    public Inventory(Product product, int totalQuantity, int minimumQuantity, String warehouseLocation) {
        this.product = product;
        this.totalQuantity = totalQuantity;
        this.minimumQuantity = minimumQuantity;
        this.warehouseLocation = warehouseLocation;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getMinimumQuantity() { return minimumQuantity; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public int getAvailableQuantity() { return totalQuantity - reservedQuantity; }

    public void addStock(int quantity) {
        totalQuantity += quantity;
        updatedAt = OffsetDateTime.now();
    }

    public void reserve(int quantity) {
        if (quantity <= 0 || quantity > getAvailableQuantity()) {
            throw new IllegalArgumentException("Quantidade indisponível para reserva.");
        }
        reservedQuantity += quantity;
        updatedAt = OffsetDateTime.now();
    }

    public void release(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity) {
            throw new IllegalArgumentException("Quantidade reservada insuficiente para liberação.");
        }
        reservedQuantity -= quantity;
        updatedAt = OffsetDateTime.now();
    }

    public void pick(int quantity) {
        if (quantity <= 0 || quantity > reservedQuantity || quantity > totalQuantity) {
            throw new IllegalArgumentException("Quantidade reservada insuficiente para picking.");
        }
        reservedQuantity -= quantity;
        totalQuantity -= quantity;
        updatedAt = OffsetDateTime.now();
    }
}
