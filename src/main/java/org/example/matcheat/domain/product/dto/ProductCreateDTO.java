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

    /** 상품/메뉴명 */
    @NotBlank
    private String productName;

    /** 최소 수주(주문) 수량 */
    @NotNull
    @Positive
    private Integer minHeadcount;

    /** 최대 수주(주문) 수량 */
    @NotNull
    @Positive
    private Integer maxHeadcount;

    /** 1인분 가격 */
    @NotNull
    @Positive
    private Integer servingPrice;

    /** 최대 배달(배송) 반경(km) */
    @NotNull
    @Positive
    private Double deliveryRadiusKm;

    /** 가게 주소. 서버가 이 값을 지오코딩해서 위경도를 계산한다 */
    @NotBlank
    private String storeAddress;

    /** 상품(음식) 카테고리 */
    @NotBlank
    private String category;

    /** 상품(음식) 설명 */
    private String description;

    /** 가게 정기 휴무 요일 */
    private DayOfWeek dayOfWeek;

    /** 가게 특정 불가 날짜 목록 */
    private List<LocalDate> unavailableDates;
}
