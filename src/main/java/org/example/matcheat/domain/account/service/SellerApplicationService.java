package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;

@Service
@Transactional(readOnly = true)
public class SellerApplicationService {
    private static final BigDecimal MAX_DELIVERY_RADIUS = new BigDecimal("999999.99");

    private final UserCredentialRepository users;
    private final SellerApplicationRepository sellerApplications;
    private final Clock clock;

    public SellerApplicationService(
            UserCredentialRepository users,
            SellerApplicationRepository sellerApplications,
            @Qualifier("accountClock") Clock clock) {
        this.users = users;
        this.sellerApplications = sellerApplications;
        this.clock = clock;
    }

    @Transactional
    public ApplicationResult apply(
            long userId,
            String rawBusinessName,
            String rawBusinessNumber,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm) {
        requireEligibleUser(userId);
        String businessName = normalizeBusinessName(rawBusinessName);
        String businessNumber = normalizeBusinessNumber(rawBusinessNumber);
        validateLocation(latitude, longitude, deliveryRadiusKm);

        if (sellerApplications.existsByUserId(userId)
                || sellerApplications.existsByBusinessNumber(businessNumber)) {
            throw alreadyExists();
        }

        try {
            SellerApplicationRepository.SellerApplication saved = sellerApplications.save(
                    userId,
                    businessName,
                    businessNumber,
                    latitude,
                    longitude,
                    deliveryRadiusKm,
                    clock.instant());
            return new ApplicationResult(saved.sellerId(), saved.status());
        } catch (DataIntegrityViolationException exception) {
            throw alreadyExists();
        }
    }

    private void requireEligibleUser(long userId) {
        UserAccount account = users.findById(userId).orElseThrow(() -> new AccountApplicationException(
                AccountErrorCode.USER_NOT_FOUND,
                "회원을 찾을 수 없습니다."));
        if (account.status() == UserStatus.SUSPENDED) {
            throw new AccountApplicationException(AccountErrorCode.ACCOUNT_SUSPENDED, "정지된 계정입니다.");
        }
        if (account.status() == UserStatus.WITHDRAWN) {
            throw new AccountApplicationException(AccountErrorCode.ACCOUNT_WITHDRAWN, "탈퇴한 계정입니다.");
        }
        if (account.role() != UserRole.USER) {
            throw new AccountApplicationException(AccountErrorCode.FORBIDDEN, "판매자 신청 권한이 없습니다.");
        }
    }

    private static String normalizeBusinessName(String businessName) {
        String normalized = businessName == null ? "" : businessName.trim();
        if (normalized.isEmpty() || normalized.length() > 100) {
            throw validationError("사업자명을 확인해 주세요.");
        }
        return normalized;
    }

    private static String normalizeBusinessNumber(String businessNumber) {
        String normalized = businessNumber == null ? "" : businessNumber.trim();
        if (!normalized.matches("[0-9 -]+")) {
            throw validationError("사업자번호 형식을 확인해 주세요.");
        }
        String digits = normalized.replaceAll("[^0-9]", "");
        if (digits.length() != 10) {
            throw validationError("사업자번호는 숫자 10자리여야 합니다.");
        }
        return digits;
    }

    private static void validateLocation(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm) {
        if ((latitude == null) != (longitude == null)) {
            throw validationError("위도와 경도는 함께 입력해야 합니다.");
        }
        if (latitude != null && (latitude.compareTo(new BigDecimal("-90")) < 0
                || latitude.compareTo(new BigDecimal("90")) > 0
                || decimalPlaces(latitude) > 7)) {
            throw validationError("위도는 -90~90 범위의 소수점 7자리 이하여야 합니다.");
        }
        if (longitude != null && (longitude.compareTo(new BigDecimal("-180")) < 0
                || longitude.compareTo(new BigDecimal("180")) > 0
                || decimalPlaces(longitude) > 7)) {
            throw validationError("경도는 -180~180 범위의 소수점 7자리 이하여야 합니다.");
        }
        if (deliveryRadiusKm != null && (deliveryRadiusKm.signum() < 0
                || deliveryRadiusKm.compareTo(MAX_DELIVERY_RADIUS) > 0
                || decimalPlaces(deliveryRadiusKm) > 2)) {
            throw validationError("배송 반경은 0 이상이며 소수점 2자리 이하여야 합니다.");
        }
    }

    private static int decimalPlaces(BigDecimal value) {
        return Math.max(0, value.stripTrailingZeros().scale());
    }

    private static AccountApplicationException validationError(String message) {
        return new AccountApplicationException(AccountErrorCode.VALIDATION_FAILED, message);
    }

    private static AccountApplicationException alreadyExists() {
        return new AccountApplicationException(
                AccountErrorCode.SELLER_APPLICATION_ALREADY_EXISTS,
                "이미 판매자 신청 정보가 존재합니다.");
    }

    public record ApplicationResult(Long sellerId, SellerVerificationStatus status) {
    }
}
