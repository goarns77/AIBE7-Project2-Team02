package org.example.matcheat.domain.order.repository;

import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.order.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * 주문 요청 Entity의 DB 저장 및 조회를 담당하는 Repository
 */
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    /**
     * 특정 상태의 주문 요청을 모두 조회
     */
    List<OrderRequest> findAllByStatus(RequestStatus status);

    /**
     * 특정 구매자가 등록한 주문을 조회
     */
    List<OrderRequest> findAllByBuyerIdOrderByIdDesc(Long buyerId);

    boolean existsByBuyerIdAndStatusIn(Long buyerId, Collection<RequestStatus> statuses);

    /**
     * 제목 또는 음식 카테고리에 검색어가 포함된 주문을 조회
     */
    @Query("""
            SELECT o
            FROM OrderRequest o
            WHERE LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(o.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<OrderRequest> searchByKeyword(
            @Param("keyword") String keyword
    );

    /**
     * 제목 또는 음식 카테고리 검색 결과를 페이지 단위로 조회한다.
     */
    @Query("""
            SELECT o
            FROM OrderRequest o
            WHERE LOWER(o.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(o.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<OrderRequest> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
