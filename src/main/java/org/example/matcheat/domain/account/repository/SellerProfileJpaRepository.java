package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.SellerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SellerProfileJpaRepository extends JpaRepository<SellerProfileEntity, Long> {
    Optional<SellerProfileEntity> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    boolean existsByBusinessNumber(String businessNumber);

    @Query("""
            select seller from SellerProfileEntity seller
            where (:status is null or seller.verificationStatus = :status)
            """)
    Page<SellerProfileEntity> search(
            @Param("status") org.example.matcheat.domain.account.enums.SellerVerificationStatus status,
            Pageable pageable);

    long countByVerificationStatus(
            org.example.matcheat.domain.account.enums.SellerVerificationStatus status);
}
