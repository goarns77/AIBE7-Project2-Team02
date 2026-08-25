package org.example.matcheat.account.domain;

import java.time.Instant;
import java.util.Objects;

public final class UserAccount {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final UserRole role;
    private final UserStatus status;
    private final int tokenVersion;
    private final Instant withdrawnAt;

    private UserAccount(
            Long id,
            String email,
            String passwordHash,
            String name,
            UserRole role,
            UserStatus status,
            int tokenVersion,
            Instant withdrawnAt) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.passwordHash = passwordHash;
        this.name = Objects.requireNonNull(name);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.tokenVersion = tokenVersion;
        this.withdrawnAt = withdrawnAt;
    }

    public static UserAccount registerUser(String email, String passwordHash, String name) {
        return new UserAccount(null, email, passwordHash, name, UserRole.USER, UserStatus.ACTIVE, 0, null);
    }

    public static UserAccount registerAdmin(String email, String passwordHash, String name) {
        return new UserAccount(null, email, passwordHash, name, UserRole.ADMIN, UserStatus.ACTIVE, 0, null);
    }

    public static UserAccount restore(
            Long id,
            String email,
            String passwordHash,
            String name,
            UserRole role,
            UserStatus status,
            int tokenVersion,
            Instant withdrawnAt) {
        return new UserAccount(id, email, passwordHash, name, role, status, tokenVersion, withdrawnAt);
    }

    public Long id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String name() {
        return name;
    }

    public UserRole role() {
        return role;
    }

    public UserStatus status() {
        return status;
    }

    public int tokenVersion() {
        return tokenVersion;
    }

    public Instant withdrawnAt() {
        return withdrawnAt;
    }
}
