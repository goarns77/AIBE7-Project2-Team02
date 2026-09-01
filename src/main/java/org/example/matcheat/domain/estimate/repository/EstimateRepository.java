package org.example.matcheat.domain.estimate.repository;

import org.example.matcheat.domain.estimate.entity.EstimateEntity;
import org.example.matcheat.domain.estimate.enums.EstimateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 견적(Estimate) Entity의 DB 저장 및 조회를 담당하는 Repository이다.
 */
public interface EstimateRepository extends JpaRepository<EstimateEntity, Long> {

    /**
     * 특정 판매자가 받은 견적 목록을 최신순으로 조회한다.
     */
    List<EstimateEntity> findAllBySellerIdOrderByIdDesc(Long sellerId);

    /**
     * 특정 주문 요청(requestId)에 달린 견적 목록을 최신순으로 조회한다.
     */
    List<EstimateEntity> findAllByRequestIdOrderByIdDesc(Long requestId);

    /**
     * 여러 주문 요청 ID에 걸쳐 달린 견적 목록을 최신순으로 조회한다. (내가 보낸 견적 조회용)
     */
    List<EstimateEntity> findAllByRequestIdInOrderByIdDesc(List<Long> requestIds);

    boolean existsBySellerIdAndStatusIn(Long sellerId, Collection<EstimateStatus> statuses);
}
