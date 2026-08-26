package org.example.matcheat.orderrequest.repository;

import org.example.matcheat.orderrequest.entity.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 주문 요청 Entity의 DB 저장 및 조회를 담당하는 Repository
 * <p>
 * OrderRequest : 이 Repository가 관리할 Entity
 * Long : OrderRequest의 PK인 id의 타입
 */
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
    /**
     * 음식 카테고리에 검색어가 포함된 주문 요청을 조회
     * findBy
     * → 조회
     * Category
     * → category 필드를 기준으로
     * Containing
     * → 검색어가 포함되어 있는지
     * IgnoreCase
     * → 영문 대소문자 구분 없이
     */
    List<OrderRequest> findByCategoryContainingIgnoreCase(String keyword);
}
