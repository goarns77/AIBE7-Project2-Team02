package org.example.matcheat.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.payment.entity.Payment;

import java.time.LocalDateTime;

/** 구매자용 영수증 — 결제 완료된 건에 대해서만 발급 의미가 있다. */
@Getter
@Builder
public class ReceiptResponse {
	private Long paymentId;
	private Long quoteId;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long amount;
	private String pgTransactionId;
	private LocalDateTime paidAt;

	public static ReceiptResponse from(Payment p) {
		if (p.getStatus() != Payment.PaymentStatus.COMPLETED) {
			throw new IllegalStateException("결제가 완료된 건만 영수증을 발급할 수 있습니다.");
		}
		return ReceiptResponse.builder()
				.paymentId(p.getId())
				.quoteId(p.getQuoteId())
				.quantity(p.getQuantity())
				.unitPrice(p.getUnitPrice())
				.deliveryFee(p.getDeliveryFee())
				.amount(p.getAmount())
				.pgTransactionId(p.getPgTransactionId())
				.paidAt(p.getPaidAt())
				.build();
	}
}