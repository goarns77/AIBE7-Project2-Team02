package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.AdminAccountRepository;

import java.time.Instant;

public record AdminUserResponse(
        Long userId,
        String email,
        String name,
        UserRole role,
        UserStatus status,
        int tokenVersion,
        Instant createdAt) {
    public static AdminUserResponse from(AdminAccountRepository.UserSummary summary) {
        return new AdminUserResponse(
                summary.userId(), summary.email(), summary.name(), summary.role(), summary.status(),
                summary.tokenVersion(), summary.createdAt());
    }
}
