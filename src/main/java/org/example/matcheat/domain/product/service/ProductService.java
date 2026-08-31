package org.example.matcheat.domain.product.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.common.location.GeocodingService;
import org.example.matcheat.domain.product.dto.ProductCreateDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.dto.ProductUpdateDTO;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.example.matcheat.domain.product.repository.ProductRepository;
import org.springframework.security.access.AccessDeniedException;
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
    private final GeocodingService geocodingService;

    /**
     * 판매 조건을 새로 생성하고 저장한 뒤 응답 DTO로 변환한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto) {
        return create(dto, null, null);
    }

    /**
     * 이미지 파일이 포함된 생성 요청도 동일한 생성 로직으로 처리한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto, MultipartFile imageFile) {
        return create(dto, imageFile, null);
    }

    /**
     * 로그인 연동 전 임시로 판매자 식별값까지 받을 수 있도록 열어둔 생성 경로이다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto, MultipartFile imageFile, Long ownerAccountId) {
        String imageUrl = storeImageOrNull(imageFile);

        GeocodingService.Coordinates coordinates =
                geocodingService.geocode(
                        dto.getStoreAddress()
                );

        ProductEntity product = ProductEntity.create(
                dto.getProductName(),
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getServingPrice(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                coordinates.latitude(),
                coordinates.longitude(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getDayOfWeek(),
                dto.getUnavailableDates(),
                imageUrl,
                ownerAccountId
        );

        ProductEntity savedProduct = productRepository.save(product);

        return ProductResponseDTO.from(savedProduct);
    }

    /**
     * 판매 조건 ID로 단건을 조회한다.
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        return ProductResponseDTO.from(product);
    }

    /**
     * 현재 로그인 사용자가 소유한 판매 조건을 조회한다.
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO findOwnedById(
            Long id,
            Long ownerAccountId
    ) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        if (product.getOwnerAccountId() == null
                || !product.getOwnerAccountId().equals(ownerAccountId)) {

            throw new AccessDeniedException(
                    "본인이 등록한 판매 조건만 제안에 사용할 수 있습니다."
            );
        }

        return ProductResponseDTO.from(product);
    }

    /**
     * 현재 로그인 사용자가 등록한 판매 조건 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findMine(Long ownerAccountId) {
        return productRepository
                .findAllByOwnerAccountIdAndHiddenFalseOrderByUpdatedAtDescIdDesc(
                        ownerAccountId
                )
                .stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    /**
     * 모든 판매 조건을 목록으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAllByHiddenFalseOrderByUpdatedAtDescIdDesc().stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    /**
     * 선택한 조건에 맞는 판매 조건만 조회한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> search(String quantity, String category, String servingPrice) {
        Integer parsedQuantity = parseNullableInteger(quantity);
        String normalizedCategory = normalizeText(category);
        Integer parsedServingPrice = parseNullableInteger(servingPrice);

        if (parsedQuantity == null && normalizedCategory == null && parsedServingPrice == null) {
            return findAll();
        }

        return productRepository.findAllByHiddenFalseOrderByUpdatedAtDescIdDesc().stream()
                .filter(product -> matchesQuantity(product, parsedQuantity))
                .filter(product -> matchesCategory(product, normalizedCategory))
                .filter(product -> matchesServingPrice(product, parsedServingPrice))
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

    /**
     * 이미지 변경이 포함된 수정 요청도 동일한 수정 로직으로 처리한다.
     */
    @Transactional
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto, MultipartFile imageFile) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        String imageUrl = product.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = storeImageOrNull(imageFile);
        }

        Double latitude = null;
        Double longitude = null;

        if (dto.getStoreAddress() != null
                && !dto.getStoreAddress().isBlank()) {

            GeocodingService.Coordinates coordinates =
                    geocodingService.geocode(
                            dto.getStoreAddress()
                    );

            latitude = coordinates.latitude();
            longitude = coordinates.longitude();
        }

        product.update(
                dto.getProductName(),
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getServingPrice(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                latitude,
                longitude,
                dto.getCategory(),
                dto.getDescription(),
                dto.getDayOfWeek(),
                dto.getUnavailableDates(),
                imageUrl
        );

        return ProductResponseDTO.from(product);
    }

    /**
     * 판매 조건을 소프트 삭제한다.
     */
    @Transactional
    public ProductResponseDTO softDelete(Long id) {
        return softDelete(id, null);
    }

    /**
     * 로그인 연동 전에는 현재는 null 허용, 이후에는 소유자 검증용 식별자를 전달받는다.
     */
    @Transactional
    public ProductResponseDTO softDelete(Long id, Long ownerAccountId) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        product.softDelete(ownerAccountId);

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

    private boolean matchesServingPrice(ProductEntity product, Integer servingPrice) {
        if (servingPrice == null) {
            return true;
        }

        return product.getServingPrice() <= servingPrice;
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
