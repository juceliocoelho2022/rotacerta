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
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "volume_m3", nullable = false, precision = 10, scale = 4)
    private BigDecimal volumeM3;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected Product() {}

    public Product(String sku, String name, String description, BigDecimal unitPrice, BigDecimal weightKg, BigDecimal volumeM3) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
        this.volumeM3 = volumeM3;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getWeightKg() { return weightKg; }
    public BigDecimal getVolumeM3() { return volumeM3; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
