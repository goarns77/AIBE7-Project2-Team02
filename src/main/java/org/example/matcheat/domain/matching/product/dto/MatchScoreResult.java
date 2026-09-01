package org.example.matcheat.domain.matching.product.dto;

import lombok.Getter;

import java.util.List;

@Getter
/**
 * 판매자-주문요청 한 쌍에 대한 소프트 매칭 점수 산출 결과를 담는 DTO이다.
 */
public class MatchScoreResult {

    private final double totalScore;
    private final List<ScoreBreakdownItem> breakdown;

    private MatchScoreResult(double totalScore, List<ScoreBreakdownItem> breakdown) {
        this.totalScore = totalScore;
        this.breakdown = breakdown;
    }

    /**
     * 항목별 점수 목록의 기여도를 합산해 총점을 계산하고 결과를 생성한다.
     */
    public static MatchScoreResult from(List<ScoreBreakdownItem> breakdown) {
        double total = breakdown.stream()
                .mapToDouble(ScoreBreakdownItem::contribution)
                .sum();

        return new MatchScoreResult(Math.round(total * 10) / 10.0, breakdown);
    }
}
