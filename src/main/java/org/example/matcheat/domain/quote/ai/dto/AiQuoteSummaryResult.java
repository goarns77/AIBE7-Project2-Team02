package org.example.matcheat.domain.quote.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Gemini가 견적서 형식으로 반환할 구조화된 응답.
 * ChatClient.entity()가 이 클래스의 필드명을 기준으로 JSON 스키마를 만들어
 * 모델에게 지시하므로, 필드명이 곧 AI에게 주는 힌트가 된다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiQuoteSummaryResult {
	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private String additionalNotes; // 구조화 불가능한 조건 정리 (없으면 빈 문자열)
}