package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.AdminAccountRepository;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminSellerApplicationResponse(
        Long sellerId,
        Long userId,
        String userEmail,
        String userName,
        String businessName,
        String businessNumber,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal deliveryRadiusKm,
        SellerVerificationStatus status,
        String rejectionReason,
        Instant appliedAt,
        Instant reviewedAt) {
    public static AdminSellerApplicationResponse from(AdminAccountRepository.SellerSummary summary) {
        return new AdminSellerApplicationResponse(
                summary.sellerId(), summary.userId(), summary.userEmail(), summary.userName(),
                summary.businessName(), summary.businessNumber(), summary.latitude(), summary.longitude(),
                summary.deliveryRadiusKm(), summary.status(), summary.rejectionReason(),
                summary.appliedAt(), summary.reviewedAt());
    }
}
