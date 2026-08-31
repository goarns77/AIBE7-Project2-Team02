package org.example.matcheat.domain.estimate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.estimate.enums.EstimateStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estimates")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    /**
     * 등록 상품을 근거로 요청하면 상품 ID가 들어가고, 직접 입력이면 null이다.
     */
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_type", nullable = false)
    private BudgetType budgetType;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @Column(name = "estimate_image", columnDefinition = "TEXT")
    private String estimateImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstimateStatus status;

    /**
     * 견적 요청을 생성하는 내부 생성자이다. 필수값 검증 후 상태를 REQUESTED로 초기화한다.
     */
    private EstimateEntity(
            String description,
            Long requestId,
            Long sellerId,
            Long productId,
            BigDecimal budget,
            BudgetType budgetType,
            String itemName,
            LocalDateTime eventDateTime,
            String estimateImage
    ) {
        validateRequiredFields(requestId, budget, budgetType, itemName, eventDateTime, sellerId);
        this.description = description;
        this.requestId = requestId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.budget = budget;
        this.budgetType = budgetType;
        this.itemName = itemName;
        this.eventDateTime = eventDateTime;
        this.estimateImage = estimateImage;
        this.status = EstimateStatus.REQUESTED;
    }

    /**
     * 새로운 견적 요청을 생성한다. requestId, sellerId는 필수이며 나머지는 서비스 계층에서
     * OrderRequest 값으로 채워질 수 있다.
     */
    public static EstimateEntity create(
            String description,
            Long requestId,
            Long sellerId,
            Long productId,
            BigDecimal budget,
            BudgetType budgetType,
            String itemName,
            LocalDateTime eventDateTime,
            String estimateImage
    ) {
        return new EstimateEntity(
                description,
                requestId,
                sellerId,
                productId,
                budget,
                budgetType,
                itemName,
                eventDateTime,
                estimateImage
        );
    }

    /**
     * 견적의 필수 필드(requestId, budget, budgetType, itemName, eventDateTime, sellerId)가
     * 모두 채워져 있는지 검증한다. 하나라도 비어 있으면 예외를 던진다.
     */
    private static void validateRequiredFields(
            Long requestId,
            BigDecimal budget,
            BudgetType budgetType,
            String itemName,
            LocalDateTime eventDateTime,
            Long sellerId
    ) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId는 필수입니다.");
        }
        if (budget == null) {
            throw new IllegalArgumentException("budget는 필수입니다.");
        }
        if (budgetType == null) {
            throw new IllegalArgumentException("budgetType은 필수입니다.");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("itemName은 필수입니다.");
        }
        if (eventDateTime == null) {
            throw new IllegalArgumentException("eventDateTime은 필수입니다.");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId는 필수입니다.");
        }
    }
}
