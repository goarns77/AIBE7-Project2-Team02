package org.example.matcheat.domain.matching.filter;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HardFilter의 주문 조건 판정 로직을 검증하는 테스트
 */
class HardFilterTest {

    private final HardFilter hardFilter = new HardFilter();

    @Test
    void 주문_수량이_판매_가능_범위에_포함되면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(30);
        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);

        boolean result = hardFilter.matchesQuantity(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 주문_수량이_최소_인원보다_적으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(5);
        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);

        boolean result = hardFilter.matchesQuantity(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 주문_수량이_최대_인원보다_많으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(60);
        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);

        boolean result = hardFilter.matchesQuantity(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 주문_카테고리와_상품_카테고리가_같으면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getCategory()).thenReturn("한식");
        when(product.getCategory()).thenReturn("한식");

        boolean result = hardFilter.matchesCategory(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 주문_카테고리와_상품_카테고리가_다르면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getCategory()).thenReturn("한식");
        when(product.getCategory()).thenReturn("양식");

        boolean result = hardFilter.matchesCategory(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 일인당_예산이_상품_일인당_가격_이상이면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.PER_PERSON);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(20000));

        when(product.getServingPrice()).thenReturn(18000);

        boolean result = hardFilter.matchesBudget(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 일인당_예산이_상품_일인당_가격보다_적으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.PER_PERSON);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(15000));

        when(product.getServingPrice()).thenReturn(18000);

        boolean result = hardFilter.matchesBudget(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 총예산이_상품_총액_이상이면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(600000));
        when(order.getQuantity()).thenReturn(30);

        when(product.getServingPrice()).thenReturn(18000);

        boolean result = hardFilter.matchesBudget(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 총예산이_상품_총액보다_적으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(500000));
        when(order.getQuantity()).thenReturn(30);

        when(product.getServingPrice()).thenReturn(18000);

        boolean result = hardFilter.matchesBudget(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 모든_하드_필터_조건을_만족하면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(30);
        when(order.getCategory()).thenReturn("한식");
        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(500000));
        when(order.getEventDateTime())
                .thenReturn(LocalDateTime.of(2026, 9, 15, 12, 0));

        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);
        when(product.getCategory()).thenReturn("한식");
        when(product.getServingPrice()).thenReturn(15000);
        when(product.getUnavailableDates())
                .thenReturn(List.of(
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12)
                ));


        boolean result = hardFilter.matches(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 하드_필터_조건_중_하나라도_만족하지_않으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(30);
        when(order.getCategory()).thenReturn("한식");
        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(500000));

        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);
        when(product.getCategory()).thenReturn("양식");
        when(product.getServingPrice()).thenReturn(15000);

        boolean result = hardFilter.matches(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 주문_행사일이_판매_불가_날짜에_포함되지_않으면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getEventDateTime())
                .thenReturn(LocalDateTime.of(2026, 9, 15, 12, 0));

        when(product.getUnavailableDates())
                .thenReturn(List.of(
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12)
                ));

        boolean result = hardFilter.matchesAvailableDate(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 주문_행사일이_판매_불가_날짜에_포함되면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getEventDateTime())
                .thenReturn(LocalDateTime.of(2026, 9, 12, 12, 0));

        when(product.getUnavailableDates())
                .thenReturn(List.of(
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 12)
                ));

        boolean result = hardFilter.matchesAvailableDate(order, product);

        assertThat(result).isFalse();
    }
}