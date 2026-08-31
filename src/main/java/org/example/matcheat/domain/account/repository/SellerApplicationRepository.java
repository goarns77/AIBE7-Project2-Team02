package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface SellerApplicationRepository {

    Optional<SellerApplication> findByUserId(long userId);

    Optional<SellerVerificationStatus> findStatusByUserId(long userId);

    boolean existsByUserId(long userId);

    boolean existsByBusinessNumber(String businessNumber);

    SellerApplication save(
            long userId,
            String businessName,
            String businessNumber,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm,
            Instant appliedAt);

    record SellerApplication(Long sellerId, SellerVerificationStatus status) {
    }
}
