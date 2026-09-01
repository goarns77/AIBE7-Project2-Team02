package org.example.matcheat.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeAccountValidationService {
    private final UserCredentialRepository users;
    private final SellerApplicationRepository sellerApplications;

    public void requireActiveUser(long userId) {
        UserAccount account = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        if (account.status() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("활성 상태의 회원만 거래할 수 있습니다.");
        }
    }

    public void requireApprovedSeller(long sellerId) {
        long userId = sellerApplications.findUserIdBySellerId(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));
        requireActiveUser(userId);
        SellerVerificationStatus status = sellerApplications.findStatusByUserId(userId).orElse(null);
        if (status != SellerVerificationStatus.APPROVED) {
            throw new AccessDeniedException("승인된 판매자만 거래할 수 있습니다.");
        }
    }

    public long approvedSellerIdForUser(long userId) {
        requireActiveUser(userId);
        var seller = sellerApplications.findByUserId(userId)
                .orElseThrow(() -> new AccessDeniedException("승인된 판매자만 거래할 수 있습니다."));
        if (seller.status() != SellerVerificationStatus.APPROVED) {
            throw new AccessDeniedException("승인된 판매자만 거래할 수 있습니다.");
        }
        return seller.sellerId();
    }
}
