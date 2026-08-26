package org.example.matcheat.domain.product.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
/**
 * 판매 조건 수정 요청에서 사용하는 입력 DTO이다.
 */
public class ProductUpdateDTO {

    @Positive
    private Integer minHeadcount;

    @Positive
    private Integer maxHeadcount;

    @Positive
    private Integer minOrderAmount;

    @Positive
    private Double deliveryRadiusKm;

    private String storeAddress;

    private String category;

    private String sellerUnavailableDates;
}
