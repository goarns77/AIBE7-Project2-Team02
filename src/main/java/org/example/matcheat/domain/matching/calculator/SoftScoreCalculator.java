package org.example.matcheat.domain.matching.calculator;

import org.example.matcheat.domain.matching.dto.MatchingResultDTO;
import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 배송 가능 판정을 통과한 판매 조건의 소프트 매칭 점수를 계산한다.
 */
@Component
public class SoftScoreCalculator {

    /**
     * 주문, 판매 조건, 실제 도로 경로를 이용해 0~100점의 매칭 점수를 계산한다.
     */
    public MatchingResultDTO calculate(
            OrderRequestResponseDTO order,
            ProductResponseDTO product,
            RouteInfo route
    ) {
        List<String> tags =
                new ArrayList<>();

        tags.add("카테고리 일치");

        double distanceKm =
                route.distanceKm();

        tags.add(
                String.format(
                        Locale.ROOT,
                        "도로 이동거리 %.1fkm",
                        distanceKm
                )
        );

        int budgetScore =
                calculateBudgetScore(
                        order,
                        product,
                        tags
                );

        int distanceScore =
                calculateDistanceScore(
                        product,
                        distanceKm,
                        tags
                );

        int capacityScore =
                calculateCapacityScore(
                        order,
                        product,
                        tags
                );

        int ratingScore =
                calculateRatingScore(
                        product,
                        tags
                );

        int totalScore =
                budgetScore
                        + distanceScore
                        + capacityScore
                        + ratingScore;

        return new MatchingResultDTO(
                product,
                totalScore,
                budgetScore,
                distanceScore,
                capacityScore,
                ratingScore,
                roundDistance(distanceKm),
                route.durationMinutes(),
                List.copyOf(tags)
        );
    }

    /**
     * 주문 예산 대비 실제 필요 금액의 여유를 35점 만점으로 계산한다.
     */
    private int calculateBudgetScore(
            OrderRequestResponseDTO order,
            ProductResponseDTO product,
            List<String> tags
    ) {
        BigDecimal requiredAmount =
                switch (order.getBudgetType()) {
                    case PER_PERSON -> BigDecimal.valueOf(
                            product.getServingPrice()
                    );

                    case TOTAL -> BigDecimal.valueOf(
                                    product.getServingPrice()
                            )
                            .multiply(
                                    BigDecimal.valueOf(
                                            order.getQuantity()
                                    )
                            );
                };

        double ratio =
                requiredAmount.divide(
                                order.getBudget(),
                                4,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue();

        if (ratio <= 0.70) {
            tags.add("예산 여유 큼");
            return 35;
        }

        if (ratio <= 0.85) {
            tags.add("예산 여유");
            return 32;
        }

        if (ratio <= 0.95) {
            tags.add("예산 적합");
            return 28;
        }

        tags.add("예산 범위 내");
        return 24;
    }

    /**
     * 판매자의 배송 가능 거리 대비 실제 도로 이동거리의 여유를 30점 만점으로 계산한다.
     */
    private int calculateDistanceScore(
            ProductResponseDTO product,
            double distanceKm,
            List<String> tags
    ) {
        double ratio =
                distanceKm
                        / product.getDeliveryRadiusKm();

        if (ratio <= 0.25) {
            tags.add("배송 거리 여유 큼");
            return 30;
        }

        if (ratio <= 0.50) {
            tags.add("배송 거리 여유");
            return 26;
        }

        if (ratio <= 0.75) {
            tags.add("배송 거리 양호");
            return 22;
        }

        tags.add("배송 가능 범위 내");
        return 18;
    }

    /**
     * 최대 수용량 대비 주문 후 남는 수량 여유를 20점 만점으로 계산한다.
     */
    private int calculateCapacityScore(
            OrderRequestResponseDTO order,
            ProductResponseDTO product,
            List<String> tags
    ) {
        int remainingCapacity =
                product.getMaxHeadcount()
                        - order.getQuantity();

        double remainingRatio =
                (double) remainingCapacity
                        / product.getMaxHeadcount();

        if (remainingRatio >= 0.50) {
            tags.add("수량 대응 여유 큼");
            return 20;
        }

        if (remainingRatio >= 0.25) {
            tags.add("수량 대응 여유");
            return 17;
        }

        if (remainingRatio >= 0.10) {
            tags.add("수량 대응 가능");
            return 14;
        }

        tags.add("주문 수량 충족");
        return 10;
    }

    /**
     * 기존 판매 평점을 15점 만점으로 계산한다.
     */
    private int calculateRatingScore(
            ProductResponseDTO product,
            List<String> tags
    ) {
        Double rating =
                product.getRatingAvg();

        // 리뷰가 없는 신규 판매자는 중립 점수를 적용한다.
        if (rating == null || rating <= 0) {
            return 8;
        }

        if (rating >= 4.5) {
            tags.add("높은 평점");
            return 15;
        }

        if (rating >= 4.0) {
            tags.add("좋은 평점");
            return 13;
        }

        if (rating >= 3.0) {
            return 10;
        }

        return 6;
    }

    private double roundDistance(
            double distanceKm
    ) {
        return BigDecimal.valueOf(distanceKm)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }
}