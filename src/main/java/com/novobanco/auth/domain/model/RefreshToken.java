package com.novobanco.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private OffsetDateTime expiresAt;
    private boolean revoked;
    private OffsetDateTime createdAt;

    public RefreshToken() {
    }

    public RefreshToken(UUID id, UUID userId, String tokenHash,
                        OffsetDateTime expiresAt, boolean revoked, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }
}
