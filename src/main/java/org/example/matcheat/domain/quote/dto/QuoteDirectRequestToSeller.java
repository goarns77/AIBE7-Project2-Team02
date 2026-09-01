package org.example.matcheat.domain.quote.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [구매자 → 판매자] 채팅방 없이 독립적으로 견적을 요청/제시할 때 쓰는 요청.
 *
 * targetSellerId는 seller_profiles.seller_id를 가리키는 대상 지정 값이지,
 * 요청자 본인의 신원이 아니다. 신원(=현재 로그인한 구매자가 누구인지)은
 * 항상 서버가 인증된 계정에서 결정한다.
 *
 * TODO: seller_profiles 도메인 합류 후 targetSellerId가 실제 존재하는
 * (그리고 승인된) 판매자인지 검증 추가.
 */
@Getter
@NoArgsConstructor
public class QuoteDirectRequestToSeller {

	@NotNull(message = "대상 판매자 ID는 필수입니다.")
	private Long targetSellerId;

	@NotNull(message = "수량은 필수입니다.")
	@Positive(message = "수량은 1개 이상이어야 합니다.")
	private Integer quantity;

	@NotNull(message = "단가는 필수입니다.")
	@Positive(message = "단가는 0보다 커야 합니다.")
	private Long unitPrice;

	private Long deliveryFee; // 선택 (null이면 0으로 계산)
}
