package org.example.matcheat.account.port.in;

import org.example.matcheat.account.domain.UserRole;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);

    record LoginCommand(String email, String password) {
    }

    record LoginResult(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserSummary user) {
    }

    record UserSummary(Long userId, String email, String name, UserRole role) {
    }
}
