package org.example.matcheat.domain.product.repository;

import org.example.matcheat.domain.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 판매 조건 엔티티에 대한 기본 CRUD 작업을 제공하는 저장소이다.
 */
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

}
