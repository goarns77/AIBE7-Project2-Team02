package org.example.matcheat.domain.product.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.product.dto.ProductCreateDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.dto.ProductUpdateDTO;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.example.matcheat.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
/**
 * 판매 조건의 생성, 조회, 수정 같은 비즈니스 로직을 처리하는 서비스이다.
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageStorageService productImageStorageService;

    /**
     * 판매 조건을 새로 생성하고 저장한 뒤 응답 DTO로 변환한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto) {
        return create(dto, null);
    }

    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto, MultipartFile imageFile) {
        String imageUrl = storeImageOrNull(imageFile);

        ProductEntity product = ProductEntity.create(
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getMinOrderAmount(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getDayOfWeek(),
                dto.getUnavailableDates(),
                imageUrl
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
     * 선택한 조건에 맞는 판매 조건만 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> search(String quantity, String category, String minOrderAmount) {
        Integer parsedQuantity = parseNullableInteger(quantity);
        String normalizedCategory = normalizeText(category);
        Integer parsedMinOrderAmount = parseNullableInteger(minOrderAmount);

        if (parsedQuantity == null && normalizedCategory == null && parsedMinOrderAmount == null) {
            return findAll();
        }

        return productRepository.findAll().stream()
                .filter(product -> matchesQuantity(product, parsedQuantity))
                .filter(product -> matchesCategory(product, normalizedCategory))
                .filter(product -> matchesMinOrderAmount(product, parsedMinOrderAmount))
                .sorted((left, right) -> {
                    int updatedAtCompare = right.getUpdatedAt().compareTo(left.getUpdatedAt());
                    if (updatedAtCompare != 0) {
                        return updatedAtCompare;
                    }

                    return Long.compare(right.getId(), left.getId());
                })
                .map(ProductResponseDTO::from)
                .toList();
    }

    /**
     * 기존 판매 조건을 전달받은 값으로 부분 수정한다.
     */
    @Transactional
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto) {
        return update(id, dto, null);
    }

    @Transactional
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto, MultipartFile imageFile) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        String imageUrl = product.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = storeImageOrNull(imageFile);
        }

        product.update(
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getMinOrderAmount(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getDayOfWeek(),
                dto.getUnavailableDates(),
                imageUrl
        );

        return ProductResponseDTO.from(product);
    }

    private Integer parseNullableInteger(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }

        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자만 입력할 수 있습니다. 입력값=%s".formatted(value), e);
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchesQuantity(ProductEntity product, Integer quantity) {
        if (quantity == null) {
            return true;
        }

        return product.getMinHeadcount() <= quantity && product.getMaxHeadcount() >= quantity;
    }

    private boolean matchesCategory(ProductEntity product, String category) {
        if (category == null) {
            return true;
        }

        return product.getCategory() != null
                && product.getCategory().toLowerCase(Locale.ROOT).contains(category.toLowerCase(Locale.ROOT));
    }

    private boolean matchesMinOrderAmount(ProductEntity product, Integer minOrderAmount) {
        if (minOrderAmount == null) {
            return true;
        }

        return product.getMinOrderAmount() <= minOrderAmount;
    }

    private String storeImageOrNull(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            return productImageStorageService.storeImage(imageFile);
        } catch (IOException e) {
            throw new IllegalStateException("상품 이미지를 저장하지 못했습니다.", e);
        }
    }
}
