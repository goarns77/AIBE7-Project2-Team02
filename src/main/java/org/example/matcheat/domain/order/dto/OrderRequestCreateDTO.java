package org.example.matcheat.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.matcheat.domain.order.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 요청 생성 API의 입력값을 전달하고 검증하는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderRequestCreateDTO {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull // 값이 반드시 있어야 함
    private LocalDateTime eventDateTime;

    @NotNull
    @Positive // 0보다 커야 함
    private Integer quantity;

    @NotNull
    private BudgetType budgetType;

    @NotNull
    @Positive
    private BigDecimal budget;

    @NotBlank // 문자열이 비어 있으면 안 됨
    private String category;

    @NotBlank
    private String deliveryAddress;
}
