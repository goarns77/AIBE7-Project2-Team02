package org.example.matcheat.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteUpdateRequest {
	private Integer quantity;   // 수정할 수량
	private Long unitPrice;      // 수정할 단가
	private Long deliveryFee;    // 수정할 배송비
}