package org.example.matcheat.account.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SellerProfileJpaRepository extends JpaRepository<SellerProfileEntity, Long> {
    boolean existsByUser_Id(Long userId);

    boolean existsByBusinessNumber(String businessNumber);
}
