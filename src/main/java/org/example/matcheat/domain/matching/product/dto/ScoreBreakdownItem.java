package org.example.matcheat.domain.matching.product.dto;

import lombok.Getter;

@Getter
/**
 * 소프트 매칭 점수 산출 시 평가 항목 하나의 점수와 근거를 담는 DTO이다.
 */
public class ScoreBreakdownItem {

    private final String label;
    private final double rawScore;
    private final double weight;
    private final String reason;

    private ScoreBreakdownItem(String label, double rawScore, double weight, String reason) {
        this.label = label;
        this.rawScore = rawScore;
        this.weight = weight;
        this.reason = reason;
    }

    /**
     * 평가 항목 하나의 점수 정보를 생성한다.
     */
    public static ScoreBreakdownItem of(String label, double rawScore, double weight, String reason) {
        return new ScoreBreakdownItem(label, rawScore, weight, reason);
    }

    /**
     * 이 항목이 최종 점수에 기여하는 값(가중치 반영)을 계산한다.
     */
    public double contribution() {
        return rawScore * weight / 100.0;
    }
}
