package org.example.matcheat.domain.product.dto;

import lombok.Getter;
import org.example.matcheat.domain.product.entity.ProductEntity;

import java.time.LocalDateTime;

@Getter
/**
 * 판매 조건 조회 응답에 사용하는 DTO이다.
 */
public class ProductResponseDTO {
    private final Long id;
    private final Integer minHeadcount;
    private final Integer maxHeadcount;
    private final Integer minOrderAmount;
    private final Double deliveryRadiusKm;
    private final String storeAddress;
    private final String category;
    private final Double ratingAvg;
    private final String sellerUnavailableDates;
    private final LocalDateTime updatedAt;

    /**
     * 엔티티 값을 응답 DTO로 옮겨 담는 생성자이다.
     */
    private ProductResponseDTO(ProductEntity product) {
        this.id = product.getId();
        this.minHeadcount = product.getMinHeadcount();
        this.maxHeadcount = product.getMaxHeadcount();
        this.minOrderAmount = product.getMinOrderAmount();
        this.deliveryRadiusKm = product.getDeliveryRadiusKm();
        this.storeAddress = product.getStoreAddress();
        this.category = product.getCategory();
        this.ratingAvg = product.getRatingAvg();
        this.sellerUnavailableDates = product.getSellerUnavailableDates();
        this.updatedAt = product.getUpdatedAt();
    }

    /**
     * 엔티티를 응답 DTO로 변환한다.
     */
    public static ProductResponseDTO from(ProductEntity product) {
        return new ProductResponseDTO(product);
    }
}
