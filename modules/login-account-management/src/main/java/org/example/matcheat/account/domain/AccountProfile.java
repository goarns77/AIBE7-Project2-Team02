package org.example.matcheat.account.domain;

public record AccountProfile(
        Long userId,
        String email,
        String name,
        UserRole role,
        UserStatus status) {
}
