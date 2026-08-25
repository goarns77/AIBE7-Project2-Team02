package org.example.matcheat.orderrequest.repository;

import org.example.matcheat.orderrequest.entity.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 주문 요청 Entity의 DB 저장 및 조회를 담당하는 Repository
 * <p>
 * OrderRequest : 이 Repository가 관리할 Entity
 * Long : OrderRequest의 PK인 id의 타입
 */
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {
}
