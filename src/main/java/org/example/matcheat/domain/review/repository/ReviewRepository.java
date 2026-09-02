package org.example.matcheat.domain.review.repository;

import org.example.matcheat.domain.review.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 리뷰(Review) Entity의 DB 저장 및 조회를 담당하는 Repository이다.
 */
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    /**
     * 이 결제 건에 이미 리뷰가 작성됐는지 확인한다. (결제 1건당 리뷰 1개 제한)
     */
    boolean existsByPaymentId(Long paymentId);

    /**
     * 특정 판매자가 받은 리뷰 목록을 최신순으로 조회한다.
     */
    List<ReviewEntity> findAllBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * 특정 상품에 달린 리뷰 목록을 조회한다. 평점 재계산에 쓰인다.
     */
    List<ReviewEntity> findAllByProductId(Long productId);
}
