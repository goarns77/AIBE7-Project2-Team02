package org.example.matcheat.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.service.TradeAccountValidationService;
import org.example.matcheat.domain.payment.entity.Payment;
import org.example.matcheat.domain.payment.entity.Settlement;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentAccessService {
    private final TradeAccountValidationService accounts;

    public void requirePaymentParticipant(Payment payment, long accountId) {
        if (payment.isPayer(accountId) || isSeller(payment.getSellerId(), accountId)) {
            return;
        }
        throw new AccessDeniedException("해당 결제 건에 접근 권한이 없습니다.");
    }

    public void requirePaymentBuyer(Payment payment, long accountId) {
        if (payment.isPayer(accountId)) {
            return;
        }
        throw new AccessDeniedException("결제 영수증은 구매자만 조회할 수 있습니다.");
    }

    public void requireSettlementSeller(Settlement settlement, long accountId) {
        if (isSeller(settlement.getSellerId(), accountId)) {
            return;
        }
        throw new AccessDeniedException("정산서는 판매자만 조회할 수 있습니다.");
    }

    private boolean isSeller(long sellerProfileId, long accountId) {
        try {
            return accounts.approvedSellerIdForUser(accountId) == sellerProfileId;
        } catch (AccessDeniedException | IllegalArgumentException exception) {
            return false;
        }
    }
}
