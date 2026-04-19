package com.novobanco.auth.infrastructure.persistence;

import com.novobanco.auth.domain.model.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", unique = true, nullable = false)
    private UUID customerId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role = "CUSTOMER";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public User toDomain() {
        return new User(id, customerId, email, password, fullName, role, active, createdAt, lastLogin);
    }

    public static UserJpaEntity fromDomain(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.id = user.getId();
        entity.customerId = user.getCustomerId();
        entity.email = user.getEmail();
        entity.password = user.getPassword();
        entity.fullName = user.getFullName();
        entity.role = user.getRole();
        entity.active = user.isActive();
        entity.createdAt = user.getCreatedAt();
        entity.lastLogin = user.getLastLogin();
        return entity;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public void setLastLogin(OffsetDateTime lastLogin) { this.lastLogin = lastLogin; }
}
