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

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "delivery_preferences")
public class DeliveryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled;

    @Column(name = "notification_channel", nullable = false, length = 30)
    private String notificationChannel;

    @Column(name = "preferred_start_time")
    private LocalTime preferredStartTime;

    @Column(name = "preferred_end_time")
    private LocalTime preferredEndTime;

    @Column(name = "delivery_instructions", length = 300)
    private String deliveryInstructions;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DeliveryPreference() {}

    public DeliveryPreference(Customer customer) {
        this.customer = customer;
        this.notificationsEnabled = true;
        this.notificationChannel = "EMAIL";
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public String getNotificationChannel() { return notificationChannel; }
    public LocalTime getPreferredStartTime() { return preferredStartTime; }
    public LocalTime getPreferredEndTime() { return preferredEndTime; }
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(
            boolean notificationsEnabled,
            String notificationChannel,
            LocalTime preferredStartTime,
            LocalTime preferredEndTime,
            String deliveryInstructions
    ) {
        this.notificationsEnabled = notificationsEnabled;
        this.notificationChannel = notificationChannel;
        this.preferredStartTime = preferredStartTime;
        this.preferredEndTime = preferredEndTime;
        this.deliveryInstructions = deliveryInstructions;
        this.updatedAt = OffsetDateTime.now();
    }
}
