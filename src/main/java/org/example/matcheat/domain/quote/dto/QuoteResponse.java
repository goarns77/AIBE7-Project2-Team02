// domain/quote/dto/QuoteResponse.java
package org.example.matcheat.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.quote.entity.Quote;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuoteResponse {

	private Long quoteId;
	private Long chatRoomId; // null일 수 있음 (채팅 없이 생성된 견적)
	private Long buyerId;
	private Long sellerId;
	private Quote.SenderRole senderRole;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private String additionalNotes; // null일 수 있음 (제안형 Quote는 보통 비어있음)
	private Quote.QuoteStatus status;
	private LocalDateTime createdAt;

	public static QuoteResponse from(Quote quote) {
		return QuoteResponse.builder()
				.quoteId(quote.getId())
				.chatRoomId(quote.getChatRoomId())
				.buyerId(quote.getBuyerId())
				.sellerId(quote.getSellerId())
				.senderRole(quote.getSenderRole())
				.quantity(quote.getQuantity())
				.unitPrice(quote.getUnitPrice())
				.deliveryFee(quote.getDeliveryFee())
				.totalAmount(quote.getTotalAmount())
				.additionalNotes(quote.getAdditionalNotes())
				.status(quote.getStatus())
				.createdAt(quote.getCreatedAt())
				.build();
	}
}