package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.service.AccountAuthService;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user) {
    public static LoginResponse from(AccountAuthService.LoginResult result) {
        AccountAuthService.UserSummary user = result.user();
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                new UserResponse(user.userId(), user.email(), user.name(), user.role()));
    }

    public record UserResponse(Long userId, String email, String name, UserRole role) {
    }
}
