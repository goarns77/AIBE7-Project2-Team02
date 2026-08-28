package org.example.matcheat.domain.product.dto;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
/**
 * 판매 조건 수정 요청에서 사용하는 입력 DTO이다.
 */
public class ProductUpdateDTO {

    private String productName;

    @Positive
    private Integer minHeadcount;

    @Positive
    private Integer maxHeadcount;

    @Positive
    private Integer servingPrice;

    @Positive
    private Double deliveryRadiusKm;

    private String storeAddress;

    private Double latitude;

    private Double longitude;

    private String category;

    private String description;

    private DayOfWeek dayOfWeek;

    private List<LocalDate> unavailableDates;
}
