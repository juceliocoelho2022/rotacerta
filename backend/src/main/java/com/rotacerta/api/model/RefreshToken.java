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
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    protected RefreshToken() {}

    public RefreshToken(AppUser user, String tokenHash, OffsetDateTime expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getRevokedAt() { return revokedAt; }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }

    public void revoke() {
        if (!revoked) {
            revoked = true;
            revokedAt = OffsetDateTime.now();
        }
    }
}
