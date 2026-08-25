package org.example.matcheat.account.port.out;

import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;

import java.util.Optional;

public interface UserCredentialRepository {
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    UserAccount save(UserAccount account);
}
