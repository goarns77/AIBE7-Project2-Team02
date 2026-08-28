package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.UserAccountEntity;
import org.example.matcheat.domain.account.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {
    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    @Query("""
            select user from UserAccountEntity user
            where (:status is null or user.status = :status)
              and (:keyword = ''
                   or lower(user.email) like lower(concat('%', :keyword, '%'))
                   or lower(user.name) like lower(concat('%', :keyword, '%')))
            """)
    Page<UserAccountEntity> search(
            @Param("keyword") String keyword,
            @Param("status") org.example.matcheat.domain.account.enums.UserStatus status,
            Pageable pageable);
}
