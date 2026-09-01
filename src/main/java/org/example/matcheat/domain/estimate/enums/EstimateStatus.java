package org.example.matcheat.domain.estimate.enums;

/**
 * 견적 요청의 진행 상태를 나타낸다. Proposal 도메인의 ProposalStatus와 같은 값 체계를 쓴다
 * (REQUESTED만 Proposal의 SENT 대신 그대로 유지 — domain/account가 이미 이 이름을 참조하고 있어서).
 * IN_TALK/ACCEPTED/REJECTED/WITHDRAWN은 아직 어디서도 set되지 않는다 — 나중에 채팅 연동이
 * 붙을 때를 대비한 값이다.
 */
public enum EstimateStatus {
    /** 구매자가 요청을 보내고 판매자의 응답을 기다리는 상태 */
    REQUESTED,
    /** 판매자와 구매자가 채팅 등으로 조건을 협의 중인 상태 (아직 미구현) */
    IN_TALK,
    /** 판매자가 요청을 수락해 견적 대응을 시작한 상태 (아직 미구현) */
    ACCEPTED,
    /** 판매자가 요청을 거절한 상태 (아직 미구현) */
    REJECTED,
    /** 구매자가 요청을 철회(취소)한 상태 (아직 미구현) */
    WITHDRAWN
}
