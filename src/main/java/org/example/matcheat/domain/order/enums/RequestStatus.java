package org.example.matcheat.domain.order.enums;

/**
 * 주문 요청서의 현재 상태를 제한된 값으로 관리
 */
public enum RequestStatus {
    MATCHING,   // 주문 요청 등록 후 판매자 제안을 모집하는 상태
    IN_TALK,    // 하나 이상의 판매자와 상담 중
    CONFIRMED,  // 최종 견적을 수락하여 주문 확정
    CANCELLED,  // 구매자가 주문 요청 취소
    CLOSED      // 주문이 성사되지 않고 모집 종료
}
