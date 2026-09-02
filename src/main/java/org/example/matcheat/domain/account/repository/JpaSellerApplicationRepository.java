package org.example.matcheat.domain.account.repository;

import org.example.matcheat.domain.account.entity.SellerProfileEntity;
import org.example.matcheat.domain.account.entity.UserAccountEntity;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JpaSellerApplicationRepository implements SellerApplicationRepository {
    private final SellerProfileJpaRepository sellerProfiles;
    private final UserAccountJpaRepository users;

    public JpaSellerApplicationRepository(
            SellerProfileJpaRepository sellerProfiles,
            UserAccountJpaRepository users) {
        this.sellerProfiles = sellerProfiles;
        this.users = users;
    }

    @Override
    public Optional<SellerApplication> findByUserId(long userId) {
        return sellerProfiles.findByUser_Id(userId)
                .map(profile -> new SellerApplication(
                        profile.id(),
                        profile.verificationStatus()
                ));
    }

    @Override
    public Optional<SellerVerificationStatus> findStatusByUserId(long userId) {
        return sellerProfiles.findByUser_Id(userId).map(SellerProfileEntity::verificationStatus);
    }

    @Override
    public Optional<Long> findUserIdBySellerId(long sellerId) {
        return sellerProfiles.findById(sellerId).map(SellerProfileEntity::userId);
    }

    @Override
    public boolean existsByUserId(long userId) {
        return sellerProfiles.existsByUser_Id(userId);
    }

    @Override
    public boolean existsByBusinessNumber(String businessNumber) {
        return sellerProfiles.existsByBusinessNumber(businessNumber);
    }

    @Override
    public SellerApplication save(
            long userId,
            String businessName,
            String businessNumber,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm,
            Instant appliedAt) {
        UserAccountEntity user = users.findById(userId).orElseThrow();
        SellerProfileEntity saved = sellerProfiles.saveAndFlush(SellerProfileEntity.apply(
                user,
                businessName,
                businessNumber,
                latitude,
                longitude,
                deliveryRadiusKm,
                appliedAt));
        return new SellerApplication(saved.id(), saved.verificationStatus());
    }
}
