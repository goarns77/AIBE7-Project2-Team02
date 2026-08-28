package org.example.matcheat.domain.product.repository;

import org.example.matcheat.domain.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 판매 조건 엔티티에 대한 기본 CRUD 작업을 제공하는 저장소이다.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * 숨김 처리되지 않은 판매 조건만 최신순으로 조회한다.
     */
    List<ProductEntity> findAllByHiddenFalseOrderByUpdatedAtDescIdDesc();

    /**
     * 숨김 처리되지 않은 판매 조건만 단건 조회한다.
     */
    Optional<ProductEntity> findByIdAndHiddenFalse(Long id);

    /**
     * 수량, 카테고리, 1인분 가격 조건에 맞는 판매 조건을 조회한다.
     */
    @Query(value = """
            SELECT *
            FROM seller_conditions p
            WHERE p.hidden = false
              AND (:quantity IS NULL OR (p.min_headcount <= :quantity AND p.max_headcount >= :quantity))
              AND (:category IS NULL OR p.category ILIKE CONCAT('%', :category, '%'))
              AND (:servingPrice IS NULL OR p.min_order_amount <= :servingPrice)
            ORDER BY p.updated_at DESC, p.id DESC
            """, nativeQuery = true)
    List<ProductEntity> search(
            @Param("quantity") Integer quantity,
            @Param("category") String category,
            @Param("servingPrice") Integer servingPrice
    );

}
