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

import java.time.OffsetDateTime;

@Entity
@Table(name = "customer_addresses")
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false, length = 160)
    private String street;

    @Column(nullable = false, length = 30)
    private String number;

    @Column(length = 120)
    private String complement;

    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 2)
    private String state;

    @Column(name = "zip_code", length = 12)
    private String zipCode;

    private Double latitude;
    private Double longitude;

    @Column(name = "primary_address", nullable = false)
    private boolean primaryAddress;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected CustomerAddress() {}

    public CustomerAddress(
            Customer customer,
            String label,
            String street,
            String number,
            String complement,
            String district,
            String city,
            String state,
            String zipCode,
            Double latitude,
            Double longitude,
            boolean primaryAddress
    ) {
        this.customer = customer;
        this.label = label;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.district = district;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.primaryAddress = primaryAddress;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getLabel() { return label; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public boolean isPrimaryAddress() { return primaryAddress; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setPrimaryAddress(boolean primaryAddress) {
        this.primaryAddress = primaryAddress;
    }
}
