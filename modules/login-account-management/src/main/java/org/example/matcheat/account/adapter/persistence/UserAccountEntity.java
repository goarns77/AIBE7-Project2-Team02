package org.example.matcheat.account.adapter.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.domain.UserStatus;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class UserAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;

    protected UserAccountEntity() {
    }

    private UserAccountEntity(UserAccount account) {
        this.id = account.id();
        this.email = account.email();
        this.passwordHash = account.passwordHash();
        this.name = account.name();
        this.role = account.role();
        this.status = account.status();
        this.tokenVersion = account.tokenVersion();
        this.withdrawnAt = account.withdrawnAt();
    }

    static UserAccountEntity fromDomain(UserAccount account) {
        return new UserAccountEntity(account);
    }

    UserAccount toDomain() {
        return UserAccount.restore(id, email, passwordHash, name, role, status, tokenVersion, withdrawnAt);
    }

    UserRole role() {
        return role;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
