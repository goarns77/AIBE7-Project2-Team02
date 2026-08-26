package org.example.matcheat.products.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.products.dto.ProductCreateDTO;
import org.example.matcheat.products.dto.ProductResponseDTO;
import org.example.matcheat.products.dto.ProductUpdateDTO;
import org.example.matcheat.products.entity.ProductEntity;
import org.example.matcheat.products.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * 판매 조건의 생성, 조회, 수정 같은 비즈니스 로직을 처리하는 서비스이다.
 */
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * 판매 조건을 새로 생성하고 저장한 뒤 응답 DTO로 변환한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto) {
        ProductEntity product = ProductEntity.create(
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getMinOrderAmount(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getCategory()
        );

        ProductEntity savedProduct = productRepository.save(product);

        return ProductResponseDTO.from(savedProduct);
    }

    /**
     * 판매 조건 ID로 단건을 조회한다.
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        return ProductResponseDTO.from(product);
    }

    /**
     * 모든 판매 조건을 목록으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    /**
     * 기존 판매 조건을 전달받은 값으로 부분 수정한다.
     */
    @Transactional
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        product.update(
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getMinOrderAmount(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getCategory()
        );

        return ProductResponseDTO.from(product);
    }
}
