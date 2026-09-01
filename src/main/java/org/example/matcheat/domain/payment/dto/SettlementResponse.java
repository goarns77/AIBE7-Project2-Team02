package org.example.matcheat.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.payment.entity.Settlement;

import java.time.LocalDateTime;

@Getter
@Builder
public class SettlementResponse {
	private Long settlementId;
	private Long paymentId;
	private Long quoteId;
	private Long sellerId;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private String additionalNotes;
	private LocalDateTime issuedAt;

	public static SettlementResponse from(Settlement s) {
		return SettlementResponse.builder()
				.settlementId(s.getId())
				.paymentId(s.getPaymentId())
				.quoteId(s.getQuoteId())
				.sellerId(s.getSellerId())
				.quantity(s.getQuantity())
				.unitPrice(s.getUnitPrice())
				.deliveryFee(s.getDeliveryFee())
				.totalAmount(s.getTotalAmount())
				.additionalNotes(s.getAdditionalNotes())
				.issuedAt(s.getIssuedAt())
				.build();
	}
}