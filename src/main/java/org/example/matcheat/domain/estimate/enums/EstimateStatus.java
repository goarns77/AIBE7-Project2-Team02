package org.example.matcheat.domain.estimate.enums;

/**
 * 견적 요청의 진행 상태를 나타낸다.
 */
public enum EstimateStatus {
    /** 구매자가 요청을 보내고 판매자의 응답을 기다리는 상태 */
    REQUESTED,
    /** 판매자가 요청을 수락해 견적 대응을 시작한 상태 */
    ACCEPTED,
    /** 판매자가 요청을 거절한 상태 */
    REJECTED,
    /** 구매자가 요청을 취소한 상태 */
    CANCELED
}
