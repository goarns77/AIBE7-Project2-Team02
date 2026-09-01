package org.example.matcheat.domain.matching.dto;

import org.example.matcheat.domain.product.dto.ProductResponseDTO;

import java.util.List;

/**
 * 판매 조건의 매칭 점수와 실제 배송 경로 및 매칭 근거를 전달한다.
 */
public record MatchingResultDTO(
        ProductResponseDTO product,
        int score,
        int budgetScore,
        int distanceScore,
        int capacityScore,
        int ratingScore,
        double routeDistanceKm,
        int routeDurationMinutes,
        List<String> tags
) {
}