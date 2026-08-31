package org.example.matcheat.domain.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.matching.calculator.SoftScoreCalculator;
import org.example.matcheat.domain.matching.dto.MatchingResultDTO;
import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.matching.filter.HardFilter;
import org.example.matcheat.domain.matching.route.RouteDistanceService;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 로컬 하드 필터, 실제 도로 거리, 소프트 점수를 조합해 매칭 결과를 생성한다.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final OrderRequestService orderRequestService;
    private final ProductService productService;
    private final HardFilter hardFilter;
    private final RouteDistanceService routeDistanceService;
    private final SoftScoreCalculator softScoreCalculator;

    /**
     * 특정 주문에 최종 매칭 가능한 판매 조건만 반환한다.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsForOrder(
            Long orderRequestId
    ) {
        OrderRequestResponseDTO order =
                orderRequestService.findById(
                        orderRequestId
                );

        return findRouteEligibleCandidates(order)
                .stream()
                .map(MatchedCandidate::product)
                .toList();
    }

    /**
     * 최종 후보의 매칭 점수를 계산하고 높은 점수순으로 반환한다.
     */
    @Transactional(readOnly = true)
    public List<MatchingResultDTO> findMatchesForOrder(
            Long orderRequestId
    ) {
        OrderRequestResponseDTO order =
                orderRequestService.findById(
                        orderRequestId
                );

        return findRouteEligibleCandidates(order)
                .stream()
                .map(candidate ->
                        softScoreCalculator.calculate(
                                order,
                                candidate.product(),
                                candidate.route()
                        )
                )
                .sorted(
                        Comparator.comparingInt(
                                        MatchingResultDTO::score
                                )
                                .reversed()
                )
                .toList();
    }

    /**
     * 로컬 조건과 실제 도로 배송 가능 거리를 모두 만족하는 후보를 조회한다.
     */
    private List<MatchedCandidate> findRouteEligibleCandidates(
            OrderRequestResponseDTO order
    ) {
        List<ProductResponseDTO> localCandidates =
                productService.findAll()
                        .stream()
                        .filter(product ->
                                hardFilter.matchesLocal(
                                        order,
                                        product
                                )
                        )
                        .filter(product ->
                                product.getLatitude() != null
                                        && product.getLongitude() != null
                        )
                        .toList();

        if (localCandidates.isEmpty()) {
            return List.of();
        }

        Map<Long, RouteInfo> routes =
                routeDistanceService.findRoutes(
                        order,
                        localCandidates
                );

        return localCandidates.stream()
                .map(product ->
                        new MatchedCandidate(
                                product,
                                routes.get(
                                        product.getId()
                                )
                        )
                )
                .filter(candidate ->
                        candidate.route() != null
                )
                .filter(candidate ->
                        candidate.route()
                                .distanceKm()
                                <= candidate.product()
                                .getDeliveryRadiusKm()
                )
                .toList();
    }

    /**
     * 판매 조건과 해당 판매 조건의 실제 도로 경로를 함께 보관한다.
     */
    private record MatchedCandidate(
            ProductResponseDTO product,
            RouteInfo route
    ) {
    }
}