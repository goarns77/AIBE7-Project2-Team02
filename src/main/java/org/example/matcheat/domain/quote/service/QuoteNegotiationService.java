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
import org.example.matcheat.domain.quote.entity.Quote;
import org.example.matcheat.domain.quote.entity.QuoteNegotiation;
import org.example.matcheat.domain.quote.repository.QuoteNegotiationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteNegotiationService {

	private final QuoteNegotiationRepository quoteNegotiationRepository;
	private final ChatService chatService;
	private final ChatMessageService chatMessageService;
	private final QuoteAiSummaryClient quoteAiSummaryClient;
	private final QuoteService quoteService; // [추가] 잠금 시 확정 Quote 발행용
	private final TransactionTemplate requiresNewTransactionTemplate; // 추가

	@Transactional
	public QuoteNegotiationResponse createInitialNegotiation(Long chatRoomId, Long currentUserId,
	                                                         QuoteNegotiationCreateRequest request) {
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);

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

		try {
			// [수정] 별도 물리 트랜잭션(REQUIRES_NEW)에서 INSERT를 시도한다.
			// 그냥 saveAndFlush만 쓰면, 실패 시 바깥 @Transactional 전체가
			// Postgres에서 "aborted" 상태가 되어 바로 아래 재조회까지 같이
			// 실패한다 (Supabase=Postgres 환경에서 실제로 재현되는 문제).
			QuoteNegotiation saved = requiresNewTransactionTemplate.execute(status ->
					quoteNegotiationRepository.saveAndFlush(negotiation));
			return QuoteNegotiationResponse.from(saved);
		} catch (DataIntegrityViolationException e) {
			// 별도 트랜잭션에서만 실패했으므로, 바깥(현재) 트랜잭션은 멀쩡하다.
			// 동시에 다른 요청이 먼저 만든 것을 재조회해서 멱등하게 반환한다.
			return quoteNegotiationRepository.findByChatRoomId(chatRoomId)
					.map(QuoteNegotiationResponse::from)
					.orElseThrow(() -> e);
		}
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

	@Transactional
	public QuoteNegotiationResponse triggerAiSummary(Long chatRoomId, Long currentUserId) {
		QuoteNegotiation negotiation = findByChatRoomIdOrThrow(chatRoomId);

		// [수정] AI 호출(비용 발생) 이전에 참여자/상태/1회제한을 전부 먼저 검증한다.
		// 예전에는 이 검증들이 AI 호출 뒤에 있어서, 이미 사용된 협상에 다시
		// 호출해도 Gemini API가 한 번 더 나간 뒤에야 예외가 났다 (테스트로 확인된 버그).
		negotiation.validateBeforeAiSummary(currentUserId);

		List<ChatMessageResponse> messages = chatMessageService.getChatHistory(chatRoomId, currentUserId);
		AiQuoteSummaryResult result = quoteAiSummaryClient.summarize(negotiation, messages);

		negotiation.applyAiSummaryResult(
				result.getQuantity(), result.getUnitPrice(), result.getDeliveryFee(), result.getAdditionalNotes());
		negotiation.markAiSummaryUsed();

		// TODO: AI 요약 완료 알림 — REST 응답으로만 받을지, 채팅 메시지처럼
		// 실시간 브로드캐스트할지는 보류. 지금은 REST 응답으로만 전달한다.

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

		// [추가] 협상 결과를 확정 Quote(status=ACCEPTED)로 발행한다.
		// 이후 Order 생성/마이페이지 조회는 제안형이든 협상형이든 이 Quote
		// 하나만 기준으로 삼는다 (합의안 4장: orders.quote_id 필수).
		Quote finalizedQuote = quoteService.createFromLockedNegotiation(negotiation);
		negotiation.markQuoteCreated(finalizedQuote.getId());

		return QuoteNegotiationResponse.from(negotiation);
	}

	private QuoteNegotiation findByChatRoomIdOrThrow(Long chatRoomId) {
		return quoteNegotiationRepository.findByChatRoomId(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("해당 채팅방의 협상 견적서를 찾을 수 없습니다. chatRoomId: " + chatRoomId));
	}
}