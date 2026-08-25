package org.example.matcheat.account.adapter.persistence;

import org.example.matcheat.account.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);
}
