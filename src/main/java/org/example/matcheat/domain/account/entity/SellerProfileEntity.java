package org.example.matcheat.domain.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "seller_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_profiles_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_seller_profiles_business_number", columnNames = "business_number")
        })
public class SellerProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccountEntity user;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(name = "business_number", nullable = false, length = 20)
    private String businessNumber;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "delivery_radius_km", precision = 8, scale = 2)
    private BigDecimal deliveryRadiusKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private SellerVerificationStatus verificationStatus;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccountEntity reviewedBy;

    @Version
    private long version;

    protected SellerProfileEntity() {
    }

    public static SellerProfileEntity apply(
            UserAccountEntity user,
            String businessName,
            String businessNumber,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm,
            Instant appliedAt) {
        SellerProfileEntity profile = new SellerProfileEntity();
        profile.user = user;
        profile.businessName = businessName;
        profile.businessNumber = businessNumber;
        profile.latitude = latitude;
        profile.longitude = longitude;
        profile.deliveryRadiusKm = deliveryRadiusKm;
        profile.verificationStatus = SellerVerificationStatus.PENDING;
        profile.appliedAt = appliedAt;
        return profile;
    }

    public Long id() {
        return id;
    }

    public SellerVerificationStatus verificationStatus() {
        return verificationStatus;
    }

    public void approve(UserAccountEntity reviewer, Instant reviewedAt) {
        verificationStatus = SellerVerificationStatus.APPROVED;
        rejectionReason = null;
        reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
    }

    public void reject(UserAccountEntity reviewer, String reason, Instant reviewedAt) {
        verificationStatus = SellerVerificationStatus.REJECTED;
        rejectionReason = reason;
        reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
    }
}
