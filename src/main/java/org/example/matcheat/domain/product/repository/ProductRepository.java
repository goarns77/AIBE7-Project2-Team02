package org.example.matcheat.domain.product.repository;

import org.example.matcheat.domain.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 판매 조건 엔티티에 대한 기본 CRUD 작업을 제공하는 저장소이다.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * 수량, 카테고리, 최소 주문 금액 조건에 맞는 판매 조건을 조회한다.
     */
    @Query(value = """
            SELECT *
            FROM seller_conditions p
            WHERE (:quantity IS NULL OR (p.min_headcount <= :quantity AND p.max_headcount >= :quantity))
              AND (:category IS NULL OR p.category ILIKE CONCAT('%', :category, '%'))
              AND (:minOrderAmount IS NULL OR p.min_order_amount <= :minOrderAmount)
            ORDER BY p.updated_at DESC, p.id DESC
            """, nativeQuery = true)
    List<ProductEntity> search(
            @Param("quantity") Integer quantity,
            @Param("category") String category,
            @Param("minOrderAmount") Integer minOrderAmount
    );

}
