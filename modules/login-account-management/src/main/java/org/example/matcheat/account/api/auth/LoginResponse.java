package org.example.matcheat.account.api.auth;

import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.port.in.LoginUseCase;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user) {
    static LoginResponse from(LoginUseCase.LoginResult result) {
        LoginUseCase.UserSummary user = result.user();
        return new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                new UserResponse(user.userId(), user.email(), user.name(), user.role()));
    }

    public record UserResponse(Long userId, String email, String name, UserRole role) {
    }
}
