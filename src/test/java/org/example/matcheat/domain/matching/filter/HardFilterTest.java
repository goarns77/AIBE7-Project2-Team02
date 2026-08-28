package org.example.matcheat.domain.matching.filter;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
    void 주문_수량이_판매_가능_최소_인원보다_적으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(5);
        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);

        boolean result = hardFilter.matchesQuantity(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 주문_수량이_판매_가능_최대_인원보다_많으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(60);
        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);

        boolean result = hardFilter.matchesQuantity(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 주문_카테고리와_판매_카테고리가_같으면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getCategory()).thenReturn("한식");
        when(product.getCategory()).thenReturn("한식");

        boolean result = hardFilter.matchesCategory(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 주문_카테고리와_판매_카테고리가_다르면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getCategory()).thenReturn("한식");
        when(product.getCategory()).thenReturn("양식");

        boolean result = hardFilter.matchesCategory(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 총예산이_최소_주문_금액_이상이면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(500000));
        when(product.getMinOrderAmount()).thenReturn(300000);

        boolean result = hardFilter.matchesMinOrderAmount(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 총예산이_최소_주문_금액보다_적으면_false를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(200000));
        when(product.getMinOrderAmount()).thenReturn(300000);

        boolean result = hardFilter.matchesMinOrderAmount(order, product);

        assertThat(result).isFalse();
    }

    @Test
    void 인당_예산과_수량을_곱한_총예산이_최소_주문_금액_이상이면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getBudgetType()).thenReturn(BudgetType.PER_PERSON);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(18000));
        when(order.getQuantity()).thenReturn(30);
        when(product.getMinOrderAmount()).thenReturn(500000);

        boolean result = hardFilter.matchesMinOrderAmount(order, product);

        assertThat(result).isTrue();
    }

    @Test
    void 모든_하드_필터_조건을_만족하면_true를_반환한다() {
        OrderRequestResponseDTO order = mock(OrderRequestResponseDTO.class);
        ProductResponseDTO product = mock(ProductResponseDTO.class);

        when(order.getQuantity()).thenReturn(30);
        when(order.getCategory()).thenReturn("한식");
        when(order.getBudgetType()).thenReturn(BudgetType.TOTAL);
        when(order.getBudget()).thenReturn(BigDecimal.valueOf(500000));

        when(product.getMinHeadcount()).thenReturn(10);
        when(product.getMaxHeadcount()).thenReturn(50);
        when(product.getCategory()).thenReturn("한식");
        when(product.getMinOrderAmount()).thenReturn(300000);

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
        when(product.getMinOrderAmount()).thenReturn(300000);

        boolean result = hardFilter.matches(order, product);

        assertThat(result).isFalse();
    }
}