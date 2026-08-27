package org.example.matcheat.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
 * 판매 조건 생성 요청에서 사용하는 입력 DTO이다.
 */
public class ProductCreateDTO {

    @NotNull
    @Positive
    private Integer minHeadcount;

    @NotNull
    @Positive
    private Integer maxHeadcount;

    @NotNull
    @Positive
    private Integer minOrderAmount;

    @NotNull
    @Positive
    private Double deliveryRadiusKm;

    @NotBlank
    private String storeAddress;

    @NotBlank
    private String category;

    private String description;

    private DayOfWeek dayOfWeek;

    private List<LocalDate> unavailableDates;
}
