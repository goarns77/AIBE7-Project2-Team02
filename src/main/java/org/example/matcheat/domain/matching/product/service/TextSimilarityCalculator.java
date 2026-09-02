package org.example.matcheat.domain.matching.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
/**
 * 판매 조건 설명과 주문 요청 설명을 Gemini 임베딩으로 비교해 의미 유사도(0~100점)를 계산한다.
 * 설명이 비어있거나 임베딩 호출이 실패하면(API 키 미설정, 네트워크 오류 등)
 * Optional.empty()를 반환해 MatchScoreCalculator가 해당 항목을 계산에서 제외하고
 * 나머지 항목으로 가중치를 재분배하도록 한다.
 */
public class TextSimilarityCalculator {

    private static final Logger log = LoggerFactory.getLogger(TextSimilarityCalculator.class);

    private final EmbeddingModel embeddingModel;

    /**
     * Gemini 임베딩 모델을 주입받는 생성자이다.
     */
    public TextSimilarityCalculator(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 두 설명 텍스트를 임베딩해 코사인 유사도를 0~100점으로 환산한다.
     * 둘 중 하나라도 비어있거나 임베딩 호출에 실패하면 빈 값을 반환한다.
     */
    public Optional<Double> similarityScore(String productDescription, String orderDescription) {
        if (isBlank(productDescription) || isBlank(orderDescription)) {
            return Optional.empty();
        }

        try {
            float[] productVector = embeddingModel.embed(productDescription);
            float[] orderVector = embeddingModel.embed(orderDescription);

            double cosineSimilarity = cosineSimilarity(productVector, orderVector);
            double score = clamp(cosineSimilarity * 100);

            return Optional.of(score);
        } catch (Exception exception) {
            // Gemini 임베딩 호출 실패는 매칭 전체를 막지 않고, 이 항목만 계산에서 제외한다.
            log.warn("텍스트 유사도 임베딩 계산에 실패했습니다. 이 항목은 매칭 점수에서 제외합니다.", exception);
            return Optional.empty();
        }
    }

    /**
     * 두 벡터 사이의 코사인 유사도를 계산한다. (-1 ~ 1 범위, 보통 텍스트 임베딩은 0 이상)
     */
    private double cosineSimilarity(float[] left, float[] right) {
        if (left.length != right.length || left.length == 0) {
            throw new IllegalArgumentException("임베딩 벡터의 차원이 일치하지 않습니다.");
        }

        double dotProduct = 0;
        double leftNorm = 0;
        double rightNorm = 0;

        for (int i = 0; i < left.length; i++) {
            dotProduct += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }

        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }

        return dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    /**
     * 문자열이 null이거나 공백뿐인지 확인한다.
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 점수를 0~100 범위로 잘라낸다.
     */
    private double clamp(double score) {
        return Math.max(0, Math.min(100, score));
    }
}
