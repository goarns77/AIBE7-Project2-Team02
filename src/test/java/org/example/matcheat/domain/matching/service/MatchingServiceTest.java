package org.example.matcheat.domain.matching.service;

import org.example.matcheat.domain.matching.calculator.SoftScoreCalculator;
import org.example.matcheat.domain.matching.dto.MatchingResultDTO;
import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.matching.filter.HardFilter;
import org.example.matcheat.domain.matching.route.RouteDistanceService;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MatchingService의 하드 필터, 도로 거리 판정, 소프트 점수 정렬을 검증한다.
 */
class MatchingServiceTest {

    private final OrderRequestService orderRequestService =
            mock(OrderRequestService.class);

    private final ProductService productService =
            mock(ProductService.class);

    private final HardFilter hardFilter =
            mock(HardFilter.class);

    private final RouteDistanceService routeDistanceService =
            mock(RouteDistanceService.class);

    private final SoftScoreCalculator softScoreCalculator =
            mock(SoftScoreCalculator.class);

    private final MatchingService matchingService =
            new MatchingService(
                    orderRequestService,
                    productService,
                    hardFilter,
                    routeDistanceService,
                    softScoreCalculator
            );

    @Test
    void 로컬_조건과_배송_가능_거리를_모두_만족한_상품만_반환한다() {
        Long orderRequestId = 1L;

        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO matchedProduct =
                mock(ProductResponseDTO.class);

        ProductResponseDTO localRejectedProduct =
                mock(ProductResponseDTO.class);

        when(orderRequestService.findById(orderRequestId))
                .thenReturn(order);

        when(productService.findAll())
                .thenReturn(
                        List.of(
                                matchedProduct,
                                localRejectedProduct
                        )
                );

        when(hardFilter.matchesLocal(order, matchedProduct))
                .thenReturn(true);

        when(hardFilter.matchesLocal(order, localRejectedProduct))
                .thenReturn(false);

        when(matchedProduct.getId())
                .thenReturn(10L);

        when(matchedProduct.getLatitude())
                .thenReturn(37.5665);

        when(matchedProduct.getLongitude())
                .thenReturn(126.9780);

        when(matchedProduct.getDeliveryRadiusKm())
                .thenReturn(10.0);

        RouteInfo route =
                new RouteInfo(
                        10L,
                        6500,
                        1200
                );

        when(
                routeDistanceService.findRoutes(
                        order,
                        List.of(matchedProduct)
                )
        ).thenReturn(
                Map.of(
                        10L,
                        route
                )
        );

        List<ProductResponseDTO> result =
                matchingService.findProductsForOrder(
                        orderRequestId
                );

        assertThat(result)
                .containsExactly(matchedProduct);
    }

    @Test
    void 실제_도로_거리가_배송_가능_반경보다_멀면_제외한다() {
        Long orderRequestId = 1L;

        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO product =
                mock(ProductResponseDTO.class);

        when(orderRequestService.findById(orderRequestId))
                .thenReturn(order);

        when(productService.findAll())
                .thenReturn(List.of(product));

        when(hardFilter.matchesLocal(order, product))
                .thenReturn(true);

        when(product.getId())
                .thenReturn(10L);

        when(product.getLatitude())
                .thenReturn(37.5665);

        when(product.getLongitude())
                .thenReturn(126.9780);

        // 판매자는 5km까지만 배송 가능하다.
        when(product.getDeliveryRadiusKm())
                .thenReturn(5.0);

        // 실제 자동차 이동거리는 6km다.
        RouteInfo route =
                new RouteInfo(
                        10L,
                        6000,
                        900
                );

        when(
                routeDistanceService.findRoutes(
                        order,
                        List.of(product)
                )
        ).thenReturn(
                Map.of(
                        10L,
                        route
                )
        );

        List<ProductResponseDTO> result =
                matchingService.findProductsForOrder(
                        orderRequestId
                );

        assertThat(result).isEmpty();
    }

    @Test
    void 최종_매칭_결과를_소프트_점수가_높은_순서로_반환한다() {
        Long orderRequestId = 1L;

        OrderRequestResponseDTO order =
                mock(OrderRequestResponseDTO.class);

        ProductResponseDTO lowerProduct =
                mock(ProductResponseDTO.class);

        ProductResponseDTO higherProduct =
                mock(ProductResponseDTO.class);

        when(orderRequestService.findById(orderRequestId))
                .thenReturn(order);

        when(productService.findAll())
                .thenReturn(
                        List.of(
                                lowerProduct,
                                higherProduct
                        )
                );

        when(hardFilter.matchesLocal(order, lowerProduct))
                .thenReturn(true);

        when(hardFilter.matchesLocal(order, higherProduct))
                .thenReturn(true);

        when(lowerProduct.getId())
                .thenReturn(10L);

        when(lowerProduct.getLatitude())
                .thenReturn(37.5665);

        when(lowerProduct.getLongitude())
                .thenReturn(126.9780);

        when(lowerProduct.getDeliveryRadiusKm())
                .thenReturn(20.0);

        when(higherProduct.getId())
                .thenReturn(20L);

        when(higherProduct.getLatitude())
                .thenReturn(37.5759);

        when(higherProduct.getLongitude())
                .thenReturn(126.9768);

        when(higherProduct.getDeliveryRadiusKm())
                .thenReturn(20.0);

        RouteInfo lowerRoute =
                new RouteInfo(
                        10L,
                        8000,
                        1200
                );

        RouteInfo higherRoute =
                new RouteInfo(
                        20L,
                        3000,
                        600
                );

        when(
                routeDistanceService.findRoutes(
                        order,
                        List.of(
                                lowerProduct,
                                higherProduct
                        )
                )
        ).thenReturn(
                Map.of(
                        10L,
                        lowerRoute,
                        20L,
                        higherRoute
                )
        );

        MatchingResultDTO lowerResult =
                new MatchingResultDTO(
                        lowerProduct,
                        75,
                        28,
                        22,
                        17,
                        8,
                        8.0,
                        20,
                        List.of("예산 적합")
                );

        MatchingResultDTO higherResult =
                new MatchingResultDTO(
                        higherProduct,
                        95,
                        32,
                        30,
                        20,
                        13,
                        3.0,
                        10,
                        List.of("배송 거리 여유")
                );

        when(
                softScoreCalculator.calculate(
                        order,
                        lowerProduct,
                        lowerRoute
                )
        ).thenReturn(lowerResult);

        when(
                softScoreCalculator.calculate(
                        order,
                        higherProduct,
                        higherRoute
                )
        ).thenReturn(higherResult);

        List<MatchingResultDTO> result =
                matchingService.findMatchesForOrder(
                        orderRequestId
                );

        assertThat(result)
                .containsExactly(
                        higherResult,
                        lowerResult
                );
    }
}