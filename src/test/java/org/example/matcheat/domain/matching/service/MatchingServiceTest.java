package org.example.matcheat.domain.matching.service;

import org.example.matcheat.domain.matching.filter.HardFilter;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MatchingService의 판매 조건 후보 조회 로직을 검증하는 테스트
 */
class MatchingServiceTest {

    private final OrderRequestService orderRequestService =
            mock(OrderRequestService.class);

    private final ProductService productService =
            mock(ProductService.class);

    private final HardFilter hardFilter =
            mock(HardFilter.class);

    private final MatchingService matchingService =
            new MatchingService(
                    orderRequestService,
                    productService,
                    hardFilter
            );

    @Test
    void 주문_조건에_맞는_판매_조건만_반환한다() {
        Long orderRequestId = 1L;

        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO matchedProduct =
                mock(ProductResponseDTO.class);

        ProductResponseDTO unmatchedProduct =
                mock(ProductResponseDTO.class);

        when(orderRequestService.findById(orderRequestId))
                .thenReturn(order);

        when(productService.findAll())
                .thenReturn(List.of(matchedProduct, unmatchedProduct));

        when(hardFilter.matches(order, matchedProduct))
                .thenReturn(true);

        when(hardFilter.matches(order, unmatchedProduct))
                .thenReturn(false);

        List<ProductResponseDTO> result =
                matchingService.findProductsForOrder(orderRequestId);

        assertThat(result).containsExactly(matchedProduct);
    }
}