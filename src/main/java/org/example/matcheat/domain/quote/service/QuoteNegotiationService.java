package org.example.matcheat.domain.quote.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.service.ChatMessageService;
import org.example.matcheat.domain.chat.service.ChatService;
import org.example.matcheat.domain.quote.ai.QuoteAiSummaryClient;
import org.example.matcheat.domain.quote.ai.dto.AiQuoteSummaryResult;
import org.example.matcheat.domain.quote.dto.QuoteNegotiationCreateRequest;
import org.example.matcheat.domain.quote.dto.QuoteNegotiationResponse;
import org.example.matcheat.domain.quote.entity.QuoteNegotiation;
import org.example.matcheat.domain.quote.repository.QuoteNegotiationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteNegotiationService {

	private final QuoteNegotiationRepository quoteNegotiationRepository;
	private final ChatService chatService; // 기존 QuoteService와 동일한 패턴 — Chat 조회는 직접 의존 허용
	private final ChatMessageService chatMessageService; // 생성자에 추가
	private final QuoteAiSummaryClient quoteAiSummaryClient; // 생성자에 추가

	@Transactional
	public QuoteNegotiationResponse createInitialNegotiation(Long chatRoomId, Long currentUserId,
	                                                         QuoteNegotiationCreateRequest request) {
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);

		// 멱등 처리: 이미 생성돼 있으면 새로 만들지 않고 기존 것을 반환한다
		// (ChatRoom 중복 생성 방지와 같은 관례)
		Optional<QuoteNegotiation> existing = quoteNegotiationRepository.findByChatRoomId(chatRoomId);
		if (existing.isPresent()) {
			return QuoteNegotiationResponse.from(existing.get());
		}

		Integer quantity = (request != null) ? request.getQuantity() : null;
		Long unitPrice = (request != null) ? request.getUnitPrice() : null;
		Long deliveryFee = (request != null) ? request.getDeliveryFee() : null;

		QuoteNegotiation negotiation = QuoteNegotiation.builder()
				.chatRoomId(chatRoomId)
				.buyerId(chatRoom.getBuyerId())
				.sellerId(chatRoom.getSellerId())
				.quantity(quantity)
				.unitPrice(unitPrice)
				.deliveryFee(deliveryFee)
				.build();

		return QuoteNegotiationResponse.from(quoteNegotiationRepository.save(negotiation));
	}

	@Transactional(readOnly = true)
	public QuoteNegotiationResponse getNegotiation(Long chatRoomId, Long currentUserId) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);
		negotiation.validateParticipant(currentUserId);
		return QuoteNegotiationResponse.from(negotiation);
	}

	@Transactional
	public QuoteNegotiationResponse editDuringNegotiation(Long chatRoomId, Long currentUserId,
	                                                      Integer quantity, Long unitPrice, Long deliveryFee) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);
		negotiation.validateFreeEdit(currentUserId);
		negotiation.applyEdit(quantity, unitPrice, deliveryFee);
		return QuoteNegotiationResponse.from(negotiation);
	}

	/**
	 * [TODO] 실제 AI 요약 호출부. Gemini(spring.ai.google.genai) 연동은 이미
	 * application.yml에 GEMINI_API_KEY로 준비되어 있음 — 채팅 이력 수집과
	 * 실제 프롬프트 설계는 아직 미구현. 지금은 "1회 제한 강제" 뼈대만 구현.
	 */
	@Transactional
	public QuoteNegotiationResponse triggerAiSummary(Long chatRoomId, Long currentUserId) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);
		negotiation.validateParticipant(currentUserId);

		List<ChatMessageResponse> messages = chatMessageService.getChatHistory(chatRoomId, currentUserId);
		AiQuoteSummaryResult result = quoteAiSummaryClient.summarize(negotiation, messages);

		negotiation.applyAiSummaryResult(
				result.getQuantity(), result.getUnitPrice(), result.getDeliveryFee(), result.getAdditionalNotes());
		negotiation.markAiSummaryUsed();

		return QuoteNegotiationResponse.from(negotiation);
	}

	@Transactional
	public QuoteNegotiationResponse editAfterAiSummary(Long chatRoomId, Long currentUserId,
	                                                   Integer quantity, Long unitPrice, Long deliveryFee) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);
		negotiation.validateFinalEdit(currentUserId);
		negotiation.applyEdit(quantity, unitPrice, deliveryFee);
		return QuoteNegotiationResponse.from(negotiation);
	}

	@Transactional
	public QuoteNegotiationResponse lockNegotiation(Long chatRoomId, Long currentUserId) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);
		negotiation.lock(currentUserId);
		// Order 생성 연동은 이번 범위에서 보류
		return QuoteNegotiationResponse.from(negotiation);
	}

	private QuoteNegotiation findByChatRoomIdOrThrow(Long chatRoomId) {
		return quoteNegotiationRepository.findByChatRoomId(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("해당 채팅방의 협상 견적서를 찾을 수 없습니다. chatRoomId: " + chatRoomId));
	}
}