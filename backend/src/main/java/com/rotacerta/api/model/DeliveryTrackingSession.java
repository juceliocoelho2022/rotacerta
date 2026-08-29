package com.rotacerta.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "delivery_tracking_sessions")
public class DeliveryTrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(name = "public_token", nullable = false, unique = true, length = 128)
    private String publicToken;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "alternate_recipient_name", length = 120)
    private String alternateRecipientName;

    @Column(name = "alternate_recipient_relationship", length = 60)
    private String alternateRecipientRelationship;

    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;

    @Column(name = "recipient_updated_at")
    private OffsetDateTime recipientUpdatedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected DeliveryTrackingSession() {}

    public DeliveryTrackingSession(OrderEntity order, String publicToken, OffsetDateTime expiresAt) {
        this.order = order;
        this.publicToken = publicToken;
        this.expiresAt = expiresAt;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public String getPublicToken() { return publicToken; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public String getAlternateRecipientName() { return alternateRecipientName; }
    public String getAlternateRecipientRelationship() { return alternateRecipientRelationship; }
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public OffsetDateTime getRecipientUpdatedAt() { return recipientUpdatedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }

    public void renew(String newToken, OffsetDateTime newExpiresAt) {
        this.publicToken = newToken;
        this.expiresAt = newExpiresAt;
        this.active = true;
    }

    public void updateRecipient(String name, String relationship, String instructions) {
        this.alternateRecipientName = name;
        this.alternateRecipientRelationship = relationship;
        this.deliveryInstructions = instructions;
        this.recipientUpdatedAt = OffsetDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}
