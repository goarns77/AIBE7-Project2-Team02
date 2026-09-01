package org.example.matcheat.domain.estimate.enums;

/**
 * 견적 요청의 진행 상태를 나타낸다.
 * ACCEPTED/REJECTED/CANCELED는 Proposal 도메인과 마찬가지로 아직 상태 전이 API가 없어
 * 현재는 어디서도 set되지 않는다 — 나중에 수락/거절/취소 기능을 붙일 때를 대비한 값이다.
 */
public enum EstimateStatus {
    /** 구매자가 요청을 보내고 판매자의 응답을 기다리는 상태 */
    REQUESTED,
    /** 판매자가 요청을 수락해 견적 대응을 시작한 상태 (아직 미구현) */
    ACCEPTED,
    /** 판매자가 요청을 거절한 상태 (아직 미구현) */
    REJECTED,
    /** 구매자가 요청을 취소한 상태 (아직 미구현) */
    CANCELED
}
