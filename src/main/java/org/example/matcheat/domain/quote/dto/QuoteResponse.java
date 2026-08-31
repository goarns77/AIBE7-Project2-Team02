// domain/quote/dto/QuoteResponse.java
package org.example.matcheat.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.quote.entity.Quote;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuoteResponse {

	private Long quoteId; // [수정 P0-2] id -> quoteId로 변경
	private Long chatRoomId;
	private Long buyerId;
	private Long sellerId;
	private Quote.SenderRole senderRole;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private Quote.QuoteStatus status;
	private LocalDateTime createdAt; // [수정 P0-3] 생성 일시 필드 추가

	public static QuoteResponse from(Quote quote) {
		return QuoteResponse.builder()
				.quoteId(quote.getId()) // [수정 P0-2]
				.chatRoomId(quote.getChatRoomId())
				.buyerId(quote.getBuyerId())
				.sellerId(quote.getSellerId())
				.senderRole(quote.getSenderRole())
				.quantity(quote.getQuantity())
				.unitPrice(quote.getUnitPrice())
				.deliveryFee(quote.getDeliveryFee())
				.totalAmount(quote.getTotalAmount())
				.status(quote.getStatus())
				.createdAt(quote.getCreatedAt()) // [수정 P0-3]
				.build();
	}
}