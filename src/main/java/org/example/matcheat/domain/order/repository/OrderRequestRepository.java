package org.example.matcheat.domain.order.repository;

import org.example.matcheat.domain.order.entity.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 주문 요청 Entity의 DB 저장 및 조회를 담당하는 Repository
 */
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

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
}