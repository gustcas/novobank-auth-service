package com.novobanco.auth.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class User {

    private UUID id;
    private UUID customerId;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLogin;

    public User() {
    }

    public User(UUID id, UUID customerId, String email, String password,
                String fullName, String role, boolean active,
                OffsetDateTime createdAt, OffsetDateTime lastLogin) {
        this.id = id;
        this.customerId = customerId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(OffsetDateTime lastLogin) { this.lastLogin = lastLogin; }
}
