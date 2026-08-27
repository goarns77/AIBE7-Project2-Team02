package org.example.matcheat.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteCreateRequest {
	private Integer quantity;   // 수량
	private Long unitPrice;     // 단가
	private Long deliveryFee;   // 배송비
}