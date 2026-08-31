package org.example.matcheat.domain.proposal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매자가 새로운 수주 제안을 등록할 때 사용하는 요청 DTO이다.
 */
@Getter
@NoArgsConstructor
public class ProposalCreateDTO {

    // 등록 상품으로 제안하면 상품 ID가 들어가고, 직접 입력이면 null이다.
    private Long productId;

    @NotBlank
    @Size(max = 100)
    private String itemName;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private Long unitPrice;

    @NotNull
    @Positive
    private Long totalAmount;

    @NotNull
    @Positive
    private Integer preparationDays;

    private String description;
}