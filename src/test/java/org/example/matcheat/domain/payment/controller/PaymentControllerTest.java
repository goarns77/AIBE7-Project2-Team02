package org.example.matcheat.domain.payment.controller;

import org.example.matcheat.domain.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentControllerTest {
    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentController controller = new PaymentController(paymentService);

    @Test
    void usesJwtSubjectForPayment() {
        controller.pay(jwt("41"), 7L);

        verify(paymentService).pay(7L, 41L);
    }

    @Test
    void usesJwtSubjectForPaymentLookup() {
        controller.get(jwt("42"), 8L);

        verify(paymentService).getByQuoteId(8L, 42L);
    }

    private static Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
    }
}
