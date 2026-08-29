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

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "order_delivery_details")
public class OrderDeliveryDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(name = "customer_address_id")
    private Long customerAddressId;

    @Column(name = "address_label", nullable = false, length = 40)
    private String addressLabel;

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

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "window_start")
    private LocalTime windowStart;

    @Column(name = "window_end")
    private LocalTime windowEnd;

    @Column(length = 300)
    private String instructions;

    protected OrderDeliveryDetails() {}

    public OrderDeliveryDetails(
            OrderEntity order,
            CustomerAddress address,
            LocalDate deliveryDate,
            LocalTime windowStart,
            LocalTime windowEnd,
            String instructions
    ) {
        this.order = order;
        this.customerAddressId = address.getId();
        this.addressLabel = address.getLabel();
        this.street = address.getStreet();
        this.number = address.getNumber();
        this.complement = address.getComplement();
        this.district = address.getDistrict();
        this.city = address.getCity();
        this.state = address.getState();
        this.zipCode = address.getZipCode();
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
        this.deliveryDate = deliveryDate;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.instructions = instructions;
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public Long getCustomerAddressId() { return customerAddressId; }
    public String getAddressLabel() { return addressLabel; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public LocalTime getWindowStart() { return windowStart; }
    public LocalTime getWindowEnd() { return windowEnd; }
    public String getInstructions() { return instructions; }
}
