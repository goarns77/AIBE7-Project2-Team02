package org.example.matcheat.domain.matching.product.service;

import org.example.matcheat.domain.matching.product.dto.MatchScoreResult;
import org.example.matcheat.domain.matching.product.dto.ScoreBreakdownItem;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
/**
 * 하드 필터를 통과한 판매자-주문요청 쌍에 대해 소프트 매칭 점수(0~100점)를 산출한다.
 * 기본 가중치는 거리 35% / 예산 25% / 카테고리 20% / 판매자 평점 10% / 설명 텍스트 유사도 10%이다.
 * 아직 값을 계산할 수 없는 항목(거리: 위경도 미입력, 텍스트 유사도: 임베딩 미구현)은
 * 이번 계산에서 제외하고, 나머지 항목의 가중치를 비율대로 재분배해 합계 100%를 맞춘다.
 */
public class MatchScoreCalculator {

    private static final double WEIGHT_DISTANCE = 35.0;
    private static final double WEIGHT_BUDGET = 25.0;
    private static final double WEIGHT_CATEGORY = 20.0;
    private static final double WEIGHT_RATING = 10.0;
    // 설명 텍스트 유사도(Gemini 임베딩 기반)는 아직 미구현이라 가중치만 정의해두고 계산에서는 제외한다.
    private static final double WEIGHT_TEXT_SIMILARITY = 10.0;

    /**
     * 판매 조건과 주문 요청 한 쌍에 대한 매칭 점수를 계산한다.
     */
    public MatchScoreResult calculate(ProductEntity product, OrderRequest orderRequest) {
        List<WeightedScore> availableScores = new ArrayList<>();

        distanceScore(product, orderRequest).ifPresent(score ->
                availableScores.add(new WeightedScore("거리 인접도", score, WEIGHT_DISTANCE, distanceReason(score)))
        );

        availableScores.add(new WeightedScore(
                "예산 적합도", budgetScore(product, orderRequest), WEIGHT_BUDGET, budgetReason(product, orderRequest)
        ));

        availableScores.add(new WeightedScore(
                "카테고리 일치도", categoryScore(product, orderRequest), WEIGHT_CATEGORY, categoryReason(product, orderRequest)
        ));

        ratingScore(product).ifPresent(score ->
                availableScores.add(new WeightedScore("판매자 평점", score, WEIGHT_RATING, ratingReason(product)))
        );

        return toResult(availableScores);
    }

    /**
     * 활성화된 항목들의 가중치 합이 100%가 되도록 비율대로 재분배한 뒤 결과 DTO로 변환한다.
     */
    private MatchScoreResult toResult(List<WeightedScore> availableScores) {
        double activeWeightSum = availableScores.stream()
                .mapToDouble(WeightedScore::weight)
                .sum();
        double scaleFactor = activeWeightSum == 0 ? 0 : 100.0 / activeWeightSum;

        List<ScoreBreakdownItem> breakdown = availableScores.stream()
                .map(item -> ScoreBreakdownItem.of(
                        item.label(),
                        item.score(),
                        item.weight() * scaleFactor,
                        item.reason()
                ))
                .toList();

        return MatchScoreResult.from(breakdown);
    }

    /**
     * 판매자와 주문 요청 좌표가 모두 있을 때만 거리 점수를 계산한다.
     * (실거리 / deliveryRadiusKm) 비율이 낮을수록 높은 점수를 준다.
     */
    private Optional<Double> distanceScore(ProductEntity product, OrderRequest orderRequest) {
        if (product.getLatitude() == null || product.getLongitude() == null
                || orderRequest.getLatitude() == null || orderRequest.getLongitude() == null) {
            return Optional.empty();
        }

        double distanceKm = haversineDistanceKm(
                product.getLatitude(), product.getLongitude(),
                orderRequest.getLatitude(), orderRequest.getLongitude()
        );

        double ratio = distanceKm / product.getDeliveryRadiusKm();

        return Optional.of(clamp((1 - ratio) * 100));
    }

    private String distanceReason(double score) {
        return "배송 반경 대비 근접도 %.0f%%".formatted(score);
    }

    /**
     * 예산이 1인분 가격 대비 얼마나 여유 있는지로 점수를 매긴다.
     * 딱 맞으면 60점, 50% 이상 여유가 있으면 100점이다.
     */
    private double budgetScore(ProductEntity product, OrderRequest orderRequest) {
        double servingPrice = product.getServingPrice();
        if (servingPrice <= 0 || orderRequest.getBudget() == null) {
            return 60;
        }

        double budget = orderRequest.getBudget().doubleValue();
        double surplusRatio = (budget - servingPrice) / servingPrice;
        double score = 60 + Math.min(Math.max(surplusRatio, 0), 0.5) / 0.5 * 40;

        return clamp(score);
    }

    private String budgetReason(ProductEntity product, OrderRequest orderRequest) {
        return "예산 적합도 %.0f%%".formatted(budgetScore(product, orderRequest));
    }

    /**
     * 카테고리가 완전히 일치하면 100점, 한쪽이 다른 쪽을 포함하는 부분 일치면 65점을 준다.
     * (완전히 무관한 카테고리는 하드 필터 단계에서 이미 제외됨)
     */
    private double categoryScore(ProductEntity product, OrderRequest orderRequest) {
        if (product.getCategory() == null || orderRequest.getCategory() == null) {
            return 50;
        }

        String productCategory = product.getCategory().trim().toLowerCase(Locale.ROOT);
        String orderCategory = orderRequest.getCategory().trim().toLowerCase(Locale.ROOT);

        return productCategory.equals(orderCategory) ? 100 : 65;
    }

    private String categoryReason(ProductEntity product, OrderRequest orderRequest) {
        return categoryScore(product, orderRequest) == 100 ? "카테고리 완전 일치" : "카테고리 부분 일치";
    }

    /**
     * 판매자 평점(5점 만점)을 100점 만점으로 환산한다.
     */
    private Optional<Double> ratingScore(ProductEntity product) {
        if (product.getRatingAvg() == null) {
            return Optional.empty();
        }

        return Optional.of(clamp(product.getRatingAvg() / 5.0 * 100));
    }

    private String ratingReason(ProductEntity product) {
        return "평점 %.1f/5.0".formatted(product.getRatingAvg());
    }

    private double clamp(double score) {
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 두 좌표 사이의 거리를 하버사인 공식으로 계산한다. (단위: km)
     */
    private double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private record WeightedScore(String label, double score, double weight, String reason) {
    }
}
