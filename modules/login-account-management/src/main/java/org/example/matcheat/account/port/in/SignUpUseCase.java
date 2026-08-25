package org.example.matcheat.account.port.in;

import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.domain.UserStatus;

public interface SignUpUseCase {
    SignUpResult signUp(SignUpCommand command);

    record SignUpCommand(String email, String password, String passwordConfirm, String name) {
    }

    record SignUpResult(Long userId, String email, String name, UserRole role, UserStatus status) {
    }
}
