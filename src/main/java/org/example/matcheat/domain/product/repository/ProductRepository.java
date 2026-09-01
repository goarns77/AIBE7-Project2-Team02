package org.example.matcheat.domain.product.repository;

import org.example.matcheat.domain.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * 특정 사용자가 등록한 숨김 상태가 아닌 판매 조건을 최신순으로 조회한다.
     */
    List<ProductEntity> findAllByOwnerAccountIdAndHiddenFalseOrderByUpdatedAtDescIdDesc(
            Long ownerAccountId
    );

}
