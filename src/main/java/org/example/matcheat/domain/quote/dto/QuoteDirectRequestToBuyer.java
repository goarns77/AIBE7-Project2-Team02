package org.example.matcheat.domain.quote.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [판매자 → 구매자] 채팅방 없이 독립적으로 견적서를 생성할 때 쓰는 요청.
 *
 * targetBuyerId는 "이 견적을 보낼 대상"이지 요청자 본인의 신원이 아니다.
 * 신원(=현재 로그인한 판매자가 누구인지)은 항상 서버가 seller_profiles를
 * 통해 인증된 계정에서 결정한다.
 *
 * 대상 계정의 존재 여부와 ACTIVE 상태는 견적 생성 서비스에서 검증한다.
 */
@Getter
@NoArgsConstructor
public class QuoteDirectRequestToBuyer {

	@NotNull(message = "대상 구매자 ID는 필수입니다.")
	private Long targetBuyerId;

	@NotNull(message = "수량은 필수입니다.")
	@Positive(message = "수량은 1개 이상이어야 합니다.")
	private Integer quantity;

	@NotNull(message = "단가는 필수입니다.")
	@Positive(message = "단가는 0보다 커야 합니다.")
	private Long unitPrice;

	private Long deliveryFee; // 선택 (null이면 0으로 계산)
}
