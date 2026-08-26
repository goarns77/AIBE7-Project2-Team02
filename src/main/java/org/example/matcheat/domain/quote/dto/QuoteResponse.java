package org.example.matcheat.domain.quote.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.quote.entity.Quote;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuoteResponse {
	private Long quoteId;
	private Long chatRoomId;
	private Long buyerId;
	private Long sellerId;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;
	private String status;
	private LocalDateTime createdAt;

	public static QuoteResponse from(Quote quote) {
		return QuoteResponse.builder()
				.quoteId(quote.getId())
				.chatRoomId(quote.getChatRoomId())
				.buyerId(quote.getBuyerId())
				.sellerId(quote.getSellerId())
				.quantity(quote.getQuantity())
				.unitPrice(quote.getUnitPrice())
				.deliveryFee(quote.getDeliveryFee())
				.totalAmount(quote.getTotalAmount())
				.status(quote.getStatus() != null ? quote.getStatus().name() : null)
				.createdAt(quote.getCreatedAt())
				.build();
	}
}