package org.example.matcheat.domain.payment.service;

import org.example.matcheat.domain.account.service.TradeAccountValidationService;
import org.example.matcheat.domain.payment.entity.Payment;
import org.example.matcheat.domain.payment.entity.Settlement;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentAccessServiceTest {
    private final TradeAccountValidationService accounts = mock(TradeAccountValidationService.class);
    private final PaymentAccessService service = new PaymentAccessService(accounts);

    @Test
    void permitsBuyerAccountAndMappedSellerAccount() {
        Payment payment = payment();
        when(accounts.approvedSellerIdForUser(20L)).thenReturn(200L);

        assertThatCode(() -> service.requirePaymentParticipant(payment, 10L)).doesNotThrowAnyException();
        assertThatCode(() -> service.requirePaymentParticipant(payment, 20L)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnrelatedAccount() {
        when(accounts.approvedSellerIdForUser(30L)).thenReturn(300L);

        assertThatThrownBy(() -> service.requirePaymentParticipant(payment(), 30L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void permitsOnlyMappedSellerAccountForSettlementAccess() {
        Settlement settlement = Settlement.builder()
                .paymentId(1L)
                .quoteId(2L)
                .buyerId(10L)
                .sellerId(200L)
                .totalAmount(1000L)
                .build();
        when(accounts.approvedSellerIdForUser(20L)).thenReturn(200L);

        assertThatCode(() -> service.requireSettlementSeller(settlement, 20L))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.requireSettlementSeller(settlement, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void permitsOnlyBuyerAccountForReceiptAccess() {
        assertThatCode(() -> service.requirePaymentBuyer(payment(), 10L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.requirePaymentBuyer(payment(), 20L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static Payment payment() {
        return Payment.builder()
                .quoteId(2L)
                .buyerId(10L)
                .sellerId(200L)
                .amount(1000L)
                .build();
    }
}
