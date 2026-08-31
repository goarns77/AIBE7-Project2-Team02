package org.example.matcheat.domain.product.dto;

import lombok.Getter;
import org.example.matcheat.domain.product.entity.ProductEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
/**
 * 판매 조건 조회 응답에 사용하는 DTO이다.
 */
public class ProductResponseDTO {
    private final Long id;
    private final Long ownerAccountId;
    private final String productName;
    private final Integer minHeadcount;
    private final Integer maxHeadcount;
    private final Integer servingPrice;
    private final Double deliveryRadiusKm;
    private final String storeAddress;
    private final Double latitude;
    private final Double longitude;
    private final String category;
    private final String description;
    private final DayOfWeek dayOfWeek;
    private final Double ratingAvg;
    private final List<LocalDate> unavailableDates;
    private final String imageUrl;
    private final boolean hidden;
    private final LocalDateTime updatedAt;

    /**
     * 엔티티 값을 응답 DTO로 옮겨 담는 생성자이다.
     */
    private ProductResponseDTO(ProductEntity product) {
        this.id = product.getId();
        this.ownerAccountId = product.getOwnerAccountId();
        this.productName = product.getProductName();
        this.minHeadcount = product.getMinHeadcount();
        this.maxHeadcount = product.getMaxHeadcount();
        this.servingPrice = product.getServingPrice();
        this.deliveryRadiusKm = product.getDeliveryRadiusKm();
        this.storeAddress = product.getStoreAddress();
        this.latitude = product.getLatitude();
        this.longitude = product.getLongitude();
        this.category = product.getCategory();
        this.description = product.getDescription();
        this.dayOfWeek = product.getDayOfWeek();
        this.ratingAvg = product.getRatingAvg();
        this.unavailableDates = List.copyOf(
                product.getUnavailableDates() == null ? List.of() : product.getUnavailableDates()
        );
        this.imageUrl = product.getImageUrl();
        this.hidden = product.isHidden();
        this.updatedAt = product.getUpdatedAt();
    }

    /**
     * 엔티티를 응답 DTO로 변환한다.
     */
    public static ProductResponseDTO from(ProductEntity product) {
        return new ProductResponseDTO(product);
    }
}
