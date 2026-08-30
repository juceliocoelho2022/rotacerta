package com.rotacerta.api.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "drone_authorization_evidence")
public class DroneAuthorizationEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "authorization_id", nullable = false)
    private DroneMissionAuthorization authorization;

    @Column(name = "evidence_type", nullable = false, length = 80)
    private String evidenceType;

    @Column(nullable = false, length = 300)
    private String reference;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected DroneAuthorizationEvidence() {}

    public DroneAuthorizationEvidence(
            DroneMissionAuthorization authorization,
            String evidenceType,
            String reference,
            String description
    ) {
        this.authorization = authorization;
        this.evidenceType = evidenceType;
        this.reference = reference;
        this.description = description;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public DroneMissionAuthorization getAuthorization() { return authorization; }
    public String getEvidenceType() { return evidenceType; }
    public String getReference() { return reference; }
    public String getDescription() { return description; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
