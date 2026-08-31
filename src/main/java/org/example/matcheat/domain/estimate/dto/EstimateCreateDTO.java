package org.example.matcheat.domain.estimate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.order.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EstimateCreateDTO {

    private String description;

    private Long productId;

    @Positive(message = "budget는 0보다 커야 합니다.")
    private BigDecimal budget;

    private BudgetType budgetType;

    private String itemName;

    private LocalDateTime eventDateTime;

    @NotNull
    private Long sellerId;

    private String estimateImage;
}
