package org.example.matcheat.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.payment.entity.Payment;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
	private Long paymentId;
	private Long quoteId;
	private Long buyerId;
	private Long sellerId;
	private Long amount;
	private Payment.PaymentStatus status;
	private String pgTransactionId;
	private LocalDateTime paidAt;
	private LocalDateTime createdAt;

	public static PaymentResponse from(Payment p) {
		return PaymentResponse.builder()
				.paymentId(p.getId())
				.quoteId(p.getQuoteId())
				.buyerId(p.getBuyerId())
				.sellerId(p.getSellerId())
				.amount(p.getAmount())
				.status(p.getStatus())
				.pgTransactionId(p.getPgTransactionId())
				.paidAt(p.getPaidAt())
				.createdAt(p.getCreatedAt())
				.build();
	}
}