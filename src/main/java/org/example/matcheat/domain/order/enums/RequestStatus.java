package org.example.matcheat.domain.order.enums;

import lombok.Getter;

/**
 * 주문 요청서의 현재 상태를 제한된 값으로 관리
 */
@Getter
public enum RequestStatus {
    MATCHING("매칭 중"),   // 주문 요청 등록 후 판매자 제안을 모집하는 상태
    IN_TALK("협의 중"),    // 하나 이상의 판매자와 상담 중
    CONFIRMED("확정"),  // 최종 견적을 수락하여 주문 확정
    CANCELLED("취소"),  // 구매자가 주문 요청 취소
    CLOSED("종료");      // 주문이 성사되지 않고 모집 종료

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }
}
