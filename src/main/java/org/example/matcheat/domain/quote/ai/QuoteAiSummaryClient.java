package org.example.matcheat.domain.quote.ai;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.quote.ai.dto.AiQuoteSummaryResult;
import org.example.matcheat.domain.quote.entity.QuoteNegotiation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuoteAiSummaryClient {

	// [확인 필요] spring-ai-google-genai 스타터가 ChatClient.Builder 빈을
	// 자동 구성해준다고 가정함. 만약 컴파일/기동 시 빈을 못 찾으면
	// @Bean ChatClient.Builder를 별도로 등록해야 함.
	private final ChatClient.Builder chatClientBuilder;

	public AiQuoteSummaryResult summarize(QuoteNegotiation negotiation, List<ChatMessageResponse> messages) {
		String chatLog = messages.stream()
				.map(m -> formatMessage(negotiation, m))
				.collect(Collectors.joining("\n"));

		String prompt = """
				너는 케이터링 주문 거래의 견적 협상 채팅 내용을 정리하는 어시스턴트다.

				[현재 1차 견적서]
				- 수량: %s
				- 단가: %s원
				- 배송비: %s원

				[채팅 전체 대화 내용]
				%s

				위 대화를 분석해서 아래 기준으로 견적서 형식의 결과를 만들어라.
				1. 대화 중 수량/단가/배송비가 실제로 합의되어 바뀐 값이 있으면 그 값으로 채워라.
				   합의된 변경이 없으면 현재 견적서 값을 그대로 유지해라. 대화에서 확인되지
				   않는 값을 추측해서 새로 채우지 마라.
				2. 수량/단가/배송비로 표현하기 어려운 추가 조건(메뉴 구성 요청, 배송 시간대,
				   알레르기·주의사항 등)이 있으면 사람이 읽기 좋은 문장으로 정리해서
				   additionalNotes에 담아라. 없으면 빈 문자열로 두어라.
				""".formatted(
				negotiation.getQuantity(), negotiation.getUnitPrice(), negotiation.getDeliveryFee(), chatLog
		);

		return chatClientBuilder.build()
				.prompt(prompt)
				.call()
				.entity(AiQuoteSummaryResult.class);
	}

	private String formatMessage(QuoteNegotiation negotiation, ChatMessageResponse m) {
		String role = m.getSenderId().equals(negotiation.getBuyerId()) ? "구매자" : "판매자";
		if (m.getMessageType() == ChatMessage.MessageType.TEXT) {
			return "[" + role + "] " + m.getMessage();
		}
		// 이미지/PDF는 내용을 읽지 못하니 첨부가 있었다는 사실만 알려준다.
		// [확인 필요] 파일 내용까지 분석해야 한다면 멀티모달 호출로 바꿔야 함 — 지금은 범위 밖으로 둠.
		return "[" + role + "] (파일 첨부: " + m.getOriginalFileName() + ")";
	}
}