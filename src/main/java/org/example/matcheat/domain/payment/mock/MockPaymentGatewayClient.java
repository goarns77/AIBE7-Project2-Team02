package org.example.matcheat.domain.payment.mock;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 실제 PG(토스페이먼츠 등) 연동 전 임시 구현. 이 클래스만 교체하면
 * PaymentService의 나머지 로직은 그대로 유지되며 실제 PG 연동 시 이 구현만 교체한다.
 * 지금은 항상 성공 처리한다.
 */
@Component
public class MockPaymentGatewayClient {

	public MockPaymentResult charge(Long quoteId, Long amount) {
		// TODO: 실제 PG 연동 시 이 메서드 내부만 교체
		String txId = "MOCK-" + UUID.randomUUID();
		return MockPaymentResult.success(txId);
	}

	public record MockPaymentResult(boolean success, String transactionId, String failureReason) {
		public static MockPaymentResult success(String txId) {
			return new MockPaymentResult(true, txId, null);
		}
	}
}
