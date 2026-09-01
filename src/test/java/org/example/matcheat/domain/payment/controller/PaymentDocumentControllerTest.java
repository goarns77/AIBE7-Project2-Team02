package org.example.matcheat.domain.payment.controller;

import org.example.matcheat.domain.payment.entity.Payment;
import org.example.matcheat.domain.payment.entity.Settlement;
import org.example.matcheat.domain.payment.service.PaymentService;
import org.example.matcheat.domain.payment.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentDocumentControllerTest {
    private final PaymentService paymentService = mock(PaymentService.class);
    private final SettlementService settlementService = mock(SettlementService.class);
    private final PaymentDocumentController controller =
            new PaymentDocumentController(paymentService, settlementService);

    @Test
    void usesJwtSubjectForReceiptLookup() {
        Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(Payment.PaymentStatus.COMPLETED);
        when(paymentService.getPaymentEntity(9L, 51L)).thenReturn(payment);

        controller.getReceipt(jwt("51"), 9L);

        verify(paymentService).getPaymentEntity(9L, 51L);
    }

    @Test
    void usesJwtSubjectForSettlementLookup() {
        Settlement settlement = mock(Settlement.class);
        when(settlementService.getByPaymentId(10L, 52L)).thenReturn(settlement);

        controller.getSettlement(jwt("52"), 10L);

        verify(settlementService).getByPaymentId(10L, 52L);
    }

    private static Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
    }
}
