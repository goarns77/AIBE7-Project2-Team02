package org.example.matcheat.domain.estimate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.estimate.entity.EstimateEntity;
import org.example.matcheat.domain.estimate.enums.EstimateStatus;
import org.example.matcheat.domain.order.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
/**
 * 견적 조회 응답에 사용하는 DTO이다.
 * requestId(구매자 계정 ID)와 sellerId(seller_profiles PK)는 내부 검증 로직에서만 쓰이고
 * JSON 응답에는 절대 노출하지 않는다 — 대신 조회하는 사람이 구매자/판매자 중 누구인지를
 * buyer/seller 불리언으로만 알려준다. ProductResponseDTO의 owner 필드와 같은 이유이다.
 */
public class EstimateResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String description;

    @JsonIgnore
    private Long requestId;

    @JsonIgnore
    private Long sellerId;

    private Long productId;
    private BigDecimal budget;
    private BudgetType budgetType;
    private String itemName;
    private Integer quantity;
    private LocalDateTime eventDateTime;
    private String estimateImage;
    private String deliveryAddress;
    private Double latitude;
    private Double longitude;
    private EstimateStatus status;

    /**
     * 조회하는 사람이 이 견적의 구매자 본인인지 여부.
     */
    private boolean buyer;

    /**
     * 조회하는 사람이 이 견적의 판매자 본인인지 여부.
     */
    private boolean seller;

    /**
     * 엔티티를 응답 DTO로 변환한다. buyer/seller 여부는 아직 모르는 상태(둘 다 false)이며,
     * {@link #withViewer(boolean, boolean)}로 나중에 채운다.
     */
    public static EstimateResponseDTO from(EstimateEntity entity) {
        return EstimateResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .requestId(entity.getRequestId())
                .sellerId(entity.getSellerId())
                .productId(entity.getProductId())
                .budget(entity.getBudget())
                .budgetType(entity.getBudgetType())
                .itemName(entity.getItemName())
                .quantity(entity.getQuantity())
                .eventDateTime(entity.getEventDateTime())
                .estimateImage(entity.getEstimateImage())
                .deliveryAddress(entity.getDeliveryAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .build();
    }

    /**
     * 조회하는 사람이 구매자/판매자 중 누구인지를 채운 뒤 자기 자신을 반환한다.
     * EstimateAccessService가 requestId/sellerId(내부용)를 계정 ID와 대조한 결과를 여기로 전달한다.
     */
    public EstimateResponseDTO withViewer(boolean isBuyer, boolean isSeller) {
        this.buyer = isBuyer;
        this.seller = isSeller;
        return this;
    }
}
