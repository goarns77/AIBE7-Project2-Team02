package org.example.matcheat.domain.product.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
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
    private final SellerApplicationRepository sellerApplicationRepository;

    /**
     * 판매 조건을 새로 생성하고 저장한 뒤 응답 DTO로 변환한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto, Long ownerAccountId) {
        return create(dto, null, ownerAccountId);
    }

    /**
     * 승인된 판매자만 판매 조건을 등록할 수 있도록 검증한 뒤 생성한다.
     */
    @Transactional
    public ProductResponseDTO create(ProductCreateDTO dto, MultipartFile imageFile, Long ownerAccountId) {
        requireApprovedSeller(ownerAccountId);

        String imageUrl = storeImageOrNull(imageFile);

        ProductEntity product = ProductEntity.create(
                dto.getProductName(),
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getServingPrice(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
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
     * 요청자가 승인(APPROVED)된 판매자인지 검증한다.
     */
    private void requireApprovedSeller(Long ownerAccountId) {
        if (ownerAccountId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        SellerVerificationStatus status = sellerApplicationRepository.findStatusByUserId(ownerAccountId)
                .orElseThrow(() -> new IllegalArgumentException("판매자 신청 정보가 없습니다."));

        if (status != SellerVerificationStatus.APPROVED) {
            throw new IllegalArgumentException("승인된 판매자만 판매 조건을 등록할 수 있습니다.");
        }
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
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto, Long ownerAccountId) {
        return update(id, dto, null, ownerAccountId);
    }

    /**
     * 이미지 변경이 포함된 수정 요청도 동일한 수정 로직으로 처리한다.
     * 요청자가 소유자인지 확인한 뒤에만 반영한다.
     */
    @Transactional
    public ProductResponseDTO update(Long id, @Valid ProductUpdateDTO dto, MultipartFile imageFile, Long ownerAccountId) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(id)
                ));

        String imageUrl = product.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = storeImageOrNull(imageFile);
        }

        product.update(
                ownerAccountId,
                dto.getProductName(),
                dto.getMinHeadcount(),
                dto.getMaxHeadcount(),
                dto.getServingPrice(),
                dto.getDeliveryRadiusKm(),
                dto.getStoreAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getDayOfWeek(),
                dto.getUnavailableDates(),
                imageUrl
        );

        return ProductResponseDTO.from(product);
    }

    /**
     * 판매 조건을 소프트 삭제한다. 요청자가 소유자인지 확인한 뒤에만 반영한다.
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

    /**
     * 문자열 파라미터를 정수로 파싱한다. 비어있으면 null, 숫자가 아니면 예외를 던진다.
     */
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

    /**
     * 문자열의 앞뒤 공백을 제거하고, 비어있으면 null로 정규화한다.
     */
    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 주문 수량이 판매 조건의 min~maxHeadcount 범위 안에 있는지 확인한다. quantity가 null이면 통과시킨다.
     */
    private boolean matchesQuantity(ProductEntity product, Integer quantity) {
        if (quantity == null) {
            return true;
        }

        return product.getMinHeadcount() <= quantity && product.getMaxHeadcount() >= quantity;
    }

    /**
     * 판매 조건의 카테고리에 검색어가 포함되는지 대소문자 구분 없이 확인한다. category가 null이면 통과시킨다.
     */
    private boolean matchesCategory(ProductEntity product, String category) {
        if (category == null) {
            return true;
        }

        return product.getCategory() != null
                && product.getCategory().toLowerCase(Locale.ROOT).contains(category.toLowerCase(Locale.ROOT));
    }

    /**
     * 판매 조건의 1인분 가격이 검색 상한선 이하인지 확인한다. servingPrice가 null이면 통과시킨다.
     */
    private boolean matchesServingPrice(ProductEntity product, Integer servingPrice) {
        if (servingPrice == null) {
            return true;
        }

        return product.getServingPrice() <= servingPrice;
    }

    /**
     * 이미지 파일이 있으면 저장소에 저장해 URL을 반환하고, 없으면 null을 반환한다.
     */
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
