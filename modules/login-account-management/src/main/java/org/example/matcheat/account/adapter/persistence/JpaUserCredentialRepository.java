package org.example.matcheat.account.adapter.persistence;

import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.port.out.DuplicateUserEmailException;
import org.example.matcheat.account.port.out.UserCredentialRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserCredentialRepository implements UserCredentialRepository {
    private final UserAccountJpaRepository repository;

    public JpaUserCredentialRepository(UserAccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmail(email).map(UserAccountEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByRole(UserRole role) {
        return repository.existsByRole(role);
    }

    @Override
    public UserAccount save(UserAccount account) {
        try {
            return repository.saveAndFlush(UserAccountEntity.fromDomain(account)).toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserEmailException(exception);
        }
    }
}
