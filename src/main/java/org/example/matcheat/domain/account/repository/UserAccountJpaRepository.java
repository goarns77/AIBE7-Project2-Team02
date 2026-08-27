package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.UserAccountEntity;
import org.example.matcheat.domain.account.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);
}
