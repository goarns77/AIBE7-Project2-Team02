package org.example.matcheat.domain.estimate.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.estimate.entity.EstimateEntity;
import org.example.matcheat.domain.estimate.enums.EstimateStatus;
import org.example.matcheat.domain.order.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EstimateResponseDTO {

    private Long id;
    private LocalDateTime createdAt;
    private String description;
    private Long requestId;
    private Long sellerId;
    private Long productId;
    private BigDecimal budget;
    private BudgetType budgetType;
    private String itemName;
    private LocalDateTime eventDateTime;
    private String estimateImage;
    private EstimateStatus status;

    /**
     * 엔티티를 응답 DTO로 변환한다.
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
                .eventDateTime(entity.getEventDateTime())
                .estimateImage(entity.getEstimateImage())
                .status(entity.getStatus())
                .build();
    }
}
