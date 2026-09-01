package org.example.matcheat.domain.matching.product.dto;

import lombok.Getter;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.entity.OrderRequest;

import java.util.List;
import java.util.Locale;

@Getter
/**
 * 판매자에게 추천되는 주문 요청 한 건을, 매칭 점수 및 근거와 함께 담는 응답 DTO이다.
 */
public class MatchedOrderResponseDTO {

    private final OrderRequestResponseDTO orderRequest;
    private final double totalScore;
    private final List<RecommendationScoreItemDTO> scoreItems;

    private MatchedOrderResponseDTO(OrderRequest orderRequest, MatchScoreResult matchScoreResult) {
        this.orderRequest = OrderRequestResponseDTO.from(orderRequest);
        this.totalScore = matchScoreResult.getTotalScore();
        this.scoreItems = matchScoreResult.getBreakdown().stream()
                .map(RecommendationScoreItemDTO::from)
                .toList();
    }

    /**
     * 주문 요청 Entity와 매칭 점수 산출 결과를 응답 DTO로 변환한다.
     */
    public static MatchedOrderResponseDTO of(OrderRequest orderRequest, MatchScoreResult matchScoreResult) {
        return new MatchedOrderResponseDTO(orderRequest, matchScoreResult);
    }

    @Getter
    public static class RecommendationScoreItemDTO {
        private final ScoreTag reasonTag;
        private final String label;
        private final double score;
        private final double weight;
        private final double contribution;
        private final String reason;

        private RecommendationScoreItemDTO(
                ScoreTag reasonTag,
                String label,
                double score,
                double weight,
                double contribution,
                String reason
        ) {
            this.reasonTag = reasonTag;
            this.label = label;
            this.score = score;
            this.weight = weight;
            this.contribution = contribution;
            this.reason = reason;
        }

        public static RecommendationScoreItemDTO from(ScoreBreakdownItem item) {
            return new RecommendationScoreItemDTO(
                    ScoreTag.fromLabel(item.getLabel()),
                    item.getLabel(),
                    item.getRawScore(),
                    item.getWeight(),
                    item.contribution(),
                    item.getReason()
            );
        }
    }

    public enum ScoreTag {
        DISTANCE,
        BUDGET,
        CATEGORY,
        RATING,
        TEXT_SIMILARITY,
        UNKNOWN;

        public static ScoreTag fromLabel(String label) {
            if (label == null) {
                return UNKNOWN;
            }

            String normalized = label.trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "거리 인접도" -> DISTANCE;
                case "예산 적합도" -> BUDGET;
                case "카테고리 일치도" -> CATEGORY;
                case "판매자 평점" -> RATING;
                case "텍스트 유사도" -> TEXT_SIMILARITY;
                default -> UNKNOWN;
            };
        }
    }
}
