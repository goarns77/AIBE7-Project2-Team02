package org.example.matcheat.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteCreateRequest {
	private Long chatRoomId;
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
}