package org.example.matcheat.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.quote.entity.QuoteNegotiation;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuoteNegotiationResponse {
	private Long negotiationId;
	private Long chatRoomId;
	private Long buyerId;
	private Long sellerId;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private String additionalNotes;
	private QuoteNegotiation.NegotiationStatus status;
	private boolean aiSummaryUsed;
	private LocalDateTime createdAt;
	private LocalDateTime lockedAt;

	public static QuoteNegotiationResponse from(QuoteNegotiation n) {
		return QuoteNegotiationResponse.builder()
				.negotiationId(n.getId())
				.chatRoomId(n.getChatRoomId())
				.buyerId(n.getBuyerId())
				.sellerId(n.getSellerId())
				.quantity(n.getQuantity())
				.unitPrice(n.getUnitPrice())
				.deliveryFee(n.getDeliveryFee())
				.totalAmount(n.getTotalAmount())
				.additionalNotes(n.getAdditionalNotes())
				.status(n.getStatus())
				.aiSummaryUsed(n.isAiSummaryUsed())
				.createdAt(n.getCreatedAt())
				.lockedAt(n.getLockedAt())
				.build();
	}
}