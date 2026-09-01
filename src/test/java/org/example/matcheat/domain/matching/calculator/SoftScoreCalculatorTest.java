package org.example.matcheat.domain.matching.calculator;

import org.example.matcheat.domain.matching.dto.MatchingResultDTO;
import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SoftScoreCalculator의 매칭 점수와 근거 태그 계산을 검증한다.
 */
class SoftScoreCalculatorTest {

    private final SoftScoreCalculator softScoreCalculator =
            new SoftScoreCalculator();

    @Test
    void 예산_거리_수량에_여유가_크면_높은_점수를_반환한다() {
        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        when(order.getBudgetType())
                .thenReturn(BudgetType.PER_PERSON);

        when(order.getBudget())
                .thenReturn(BigDecimal.valueOf(20000));

        when(order.getQuantity())
                .thenReturn(20);

        when(product.getServingPrice())
                .thenReturn(15000);

        when(product.getDeliveryRadiusKm())
                .thenReturn(20.0);

        when(product.getMaxHeadcount())
                .thenReturn(50);

        // 리뷰가 없는 신규 판매자는 중립 점수 8점을 받는다.
        when(product.getRatingAvg())
                .thenReturn(null);

        RouteInfo route =
                new RouteInfo(
                        2L,
                        3000,
                        600
                );

        MatchingResultDTO result =
                softScoreCalculator.calculate(
                        order,
                        product,
                        route
                );

        assertThat(result.score())
                .isEqualTo(90);

        assertThat(result.budgetScore())
                .isEqualTo(32);

        assertThat(result.distanceScore())
                .isEqualTo(30);

        assertThat(result.capacityScore())
                .isEqualTo(20);

        assertThat(result.ratingScore())
                .isEqualTo(8);

        assertThat(result.routeDistanceKm())
                .isEqualTo(3.0);

        assertThat(result.routeDurationMinutes())
                .isEqualTo(10);

        assertThat(result.tags())
                .contains(
                        "카테고리 일치",
                        "도로 이동거리 3.0km",
                        "예산 여유",
                        "배송 거리 여유 큼",
                        "수량 대응 여유 큼"
                );
    }

    @Test
    void 배송_가능_거리와_예산_한계에_가까우면_점수가_낮아진다() {
        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        when(order.getBudgetType())
                .thenReturn(BudgetType.PER_PERSON);

        when(order.getBudget())
                .thenReturn(BigDecimal.valueOf(20000));

        when(order.getQuantity())
                .thenReturn(45);

        when(product.getServingPrice())
                .thenReturn(19500);

        when(product.getDeliveryRadiusKm())
                .thenReturn(20.0);

        when(product.getMaxHeadcount())
                .thenReturn(50);

        when(product.getRatingAvg())
                .thenReturn(4.6);

        RouteInfo route =
                new RouteInfo(
                        2L,
                        18000,
                        2400
                );

        MatchingResultDTO result =
                softScoreCalculator.calculate(
                        order,
                        product,
                        route
                );

        assertThat(result.score())
                .isEqualTo(71);

        assertThat(result.budgetScore())
                .isEqualTo(24);

        assertThat(result.distanceScore())
                .isEqualTo(18);

        assertThat(result.capacityScore())
                .isEqualTo(14);

        assertThat(result.ratingScore())
                .isEqualTo(15);

        assertThat(result.tags())
                .contains(
                        "예산 범위 내",
                        "배송 가능 범위 내",
                        "수량 대응 가능",
                        "높은 평점"
                );
    }

    @Test
    void 총예산_주문도_전체_필요금액을_기준으로_예산점수를_계산한다() {
        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        when(order.getBudgetType())
                .thenReturn(BudgetType.TOTAL);

        when(order.getBudget())
                .thenReturn(BigDecimal.valueOf(400000));

        when(order.getQuantity())
                .thenReturn(20);

        // 15,000원 × 20명 = 300,000원
        when(product.getServingPrice())
                .thenReturn(15000);

        when(product.getDeliveryRadiusKm())
                .thenReturn(20.0);

        when(product.getMaxHeadcount())
                .thenReturn(50);

        when(product.getRatingAvg())
                .thenReturn(null);

        RouteInfo route =
                new RouteInfo(
                        2L,
                        5000,
                        900
                );

        MatchingResultDTO result =
                softScoreCalculator.calculate(
                        order,
                        product,
                        route
                );

        // 필요금액 / 예산 = 300,000 / 400,000 = 0.75
        assertThat(result.budgetScore())
                .isEqualTo(32);

        assertThat(result.tags())
                .contains("예산 여유");
    }
}