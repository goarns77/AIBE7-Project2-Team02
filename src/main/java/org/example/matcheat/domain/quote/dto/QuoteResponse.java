package org.example.matcheat.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.quote.entity.Quote;

@Getter
@Builder
public class QuoteResponse {

	private Long id;
	private Long chatRoomId;
	private Long buyerId;
	private Long sellerId;
	private Quote.SenderRole senderRole; // [P1-4 추가]
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private Quote.QuoteStatus status;

	public static QuoteResponse from(Quote quote) {
		return QuoteResponse.builder()
				.id(quote.getId())
				.chatRoomId(quote.getChatRoomId())
				.buyerId(quote.getBuyerId())
				.sellerId(quote.getSellerId())
				.senderRole(quote.getSenderRole()) // [P1-4 추가]
				.quantity(quote.getQuantity())
				.unitPrice(quote.getUnitPrice())
				.deliveryFee(quote.getDeliveryFee())
				.totalAmount(quote.getTotalAmount())
				.status(quote.getStatus())
				.build();
	}
}