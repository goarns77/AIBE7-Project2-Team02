package org.example.matcheat.orderrequest.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.matcheat.orderrequest.enums.BudgetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 요청에서 전달된 항목만 부분 수정하기 위한 입력 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderRequestUpdateDTO {

    private LocalDateTime eventDateTime;

    @Positive
    private Integer quantity;

    private BudgetType budgetType;

    @Positive
    private BigDecimal budget;

    private String category;

    private String deliveryAddress;

    private Double latitude;

    private Double longitude;
}
