package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.service.AccountAuthService;

public record SignUpResponse(
        Long userId,
        String email,
        String name,
        UserRole role,
        UserStatus status) {
    public static SignUpResponse from(AccountAuthService.SignUpResult result) {
        return new SignUpResponse(result.userId(), result.email(), result.name(), result.role(), result.status());
    }
}
