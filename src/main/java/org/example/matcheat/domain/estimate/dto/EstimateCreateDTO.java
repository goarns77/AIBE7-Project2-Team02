package org.example.matcheat.domain.estimate.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotNull
    private Long productId;

    @NotNull
    @Positive(message = "budget는 0보다 커야 합니다.")
    private BigDecimal budget;

    @NotNull
    private BudgetType budgetType;

    @NotBlank
    private String itemName;

    @NotNull
    @Positive(message = "quantity는 0보다 커야 합니다.")
    private Integer quantity;

    @NotNull
    private LocalDateTime eventDateTime;

    @NotBlank
    private String deliveryAddress;

    private String estimateImage;
}
