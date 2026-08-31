package org.example.matcheat.domain.quote.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1차 견적서 생성 요청. 필드 전체가 선택값이다 — 아직 주문요청/상품 모듈이
 * 없어 초기값을 못 채우는 경우가 있기 때문. 그 모듈이 준비되면 여기 값들을
 * 채워서 호출하면 된다.
 */
@Getter
@NoArgsConstructor
public class QuoteNegotiationCreateRequest {
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
}