package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.SellerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SellerProfileJpaRepository extends JpaRepository<SellerProfileEntity, Long> {
    Optional<SellerProfileEntity> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    boolean existsByBusinessNumber(String businessNumber);
}
