package com.rotacerta.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_name", nullable = false, length = 180)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Column(name = "volume_m3", nullable = false, precision = 10, scale = 4)
    private BigDecimal volumeM3;

    protected OrderItem() {}

    public OrderItem(
            OrderEntity order,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal weightKg,
            BigDecimal volumeM3
    ) {
        this.order = order;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.weightKg = weightKg;
        this.volumeM3 = volumeM3;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getWeightKg() { return weightKg; }
    public BigDecimal getVolumeM3() { return volumeM3; }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
