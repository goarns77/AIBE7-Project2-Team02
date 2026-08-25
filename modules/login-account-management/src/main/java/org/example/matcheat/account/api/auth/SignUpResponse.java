package org.example.matcheat.account.api.auth;

import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.domain.UserStatus;
import org.example.matcheat.account.port.in.SignUpUseCase;

public record SignUpResponse(
        Long userId,
        String email,
        String name,
        UserRole role,
        UserStatus status) {
    static SignUpResponse from(SignUpUseCase.SignUpResult result) {
        return new SignUpResponse(result.userId(), result.email(), result.name(), result.role(), result.status());
    }
}
