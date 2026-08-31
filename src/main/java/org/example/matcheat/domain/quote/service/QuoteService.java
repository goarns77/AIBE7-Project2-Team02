// domain/quote/service/QuoteService.java
package org.example.matcheat.domain.quote.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.service.ChatService;
import org.example.matcheat.domain.quote.dto.QuoteCreateRequest;
import org.example.matcheat.domain.quote.dto.QuoteDirectRequestToBuyer;
import org.example.matcheat.domain.quote.dto.QuoteDirectRequestToSeller;
import org.example.matcheat.domain.quote.dto.QuoteResponse;
import org.example.matcheat.domain.quote.dto.QuoteUpdateRequest;
import org.example.matcheat.domain.quote.entity.Quote;
import org.example.matcheat.domain.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuoteService {

	private final QuoteRepository quoteRepository;
	private final ChatService chatService;

	// -----------------------------------------------------------
	// 생성 - 채팅방 자동 생성 (기존 흐름: 구매자가 판매자를 지정해 견적 요청)
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse createQuoteWithNewChatRoom(Long currentBuyerId, Long sellerId, QuoteCreateRequest request) {
		ChatRoom chatRoom = chatService.getOrCreateChatRoomForQuote(
				null, ChatRoom.OriginType.PROPOSAL, currentBuyerId, sellerId
		);

		Quote.SenderRole senderRole = currentBuyerId.equals(chatRoom.getBuyerId())
				? Quote.SenderRole.BUYER
				: Quote.SenderRole.SELLER;

		Quote quote = buildQuoteFromChatRoom(
				chatRoom.getId(), chatRoom.getBuyerId(), chatRoom.getSellerId(), senderRole, request
		);

		Quote savedQuote = quoteRepository.save(quote);
		chatRoom.updateQuoteId(savedQuote.getId());

		return QuoteResponse.from(savedQuote);
	}

	// -----------------------------------------------------------
	// 생성 - 기존 채팅방 안에서 (참여자 검증 포함)
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse createQuoteInChatRoom(Long chatRoomId, Long currentUserId, QuoteCreateRequest request) {
		ChatRoom chatRoom = chatService.getChatRoomEntity(chatRoomId);
		chatRoom.validateParticipant(currentUserId);

		Quote.SenderRole senderRole = currentUserId.equals(chatRoom.getBuyerId())
				? Quote.SenderRole.BUYER
				: Quote.SenderRole.SELLER;

		Quote quote = buildQuoteFromChatRoom(
				chatRoomId, chatRoom.getBuyerId(), chatRoom.getSellerId(), senderRole, request
		);

		Quote savedQuote = quoteRepository.save(quote);
		chatRoom.updateQuoteId(savedQuote.getId());

		return QuoteResponse.from(savedQuote);
	}

	// -----------------------------------------------------------
	// 생성 - 채팅방 없이, 판매자 → 구매자
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse createStandaloneQuoteToBuyer(Long currentSellerId, QuoteDirectRequestToBuyer request) {
		// TODO: 회원 도메인 합류 후 targetBuyerId 존재 여부 검증 추가
		Quote quote = buildStandaloneQuote(
				request.getTargetBuyerId(), currentSellerId, Quote.SenderRole.SELLER,
				request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee()
		);
		return QuoteResponse.from(quoteRepository.save(quote));
	}

	// -----------------------------------------------------------
	// 생성 - 채팅방 없이, 구매자 → 판매자
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse createStandaloneQuoteToSeller(Long currentBuyerId, QuoteDirectRequestToSeller request) {
		// TODO: seller_profiles 도메인 합류 후 targetSellerId 존재/승인 여부 검증 추가
		Quote quote = buildStandaloneQuote(
				currentBuyerId, request.getTargetSellerId(), Quote.SenderRole.BUYER,
				request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee()
		);
		return QuoteResponse.from(quoteRepository.save(quote));
	}

	// -----------------------------------------------------------
	// 조회 - 참여자만 가능
	// -----------------------------------------------------------
	@Transactional(readOnly = true)
	public QuoteResponse getQuote(Long quoteId, Long currentUserId) {
		Quote quote = findQuoteOrThrow(quoteId);
		quote.validateParticipant(currentUserId);
		return QuoteResponse.from(quote);
	}

	// -----------------------------------------------------------
	// 수정 - 보낸 당사자만 가능 (SENT 상태에서만, 엔티티가 검증)
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse updateQuote(Long quoteId, Long currentUserId, QuoteUpdateRequest request) {
		Quote quote = findQuoteOrThrow(quoteId);
		quote.validateSenderOnly(currentUserId);

		long totalAmount = Quote.calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());
		quote.updateQuoteDetails(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee(), totalAmount);

		return QuoteResponse.from(quote);
	}

	// -----------------------------------------------------------
	// 상태 변경 - ACCEPTED/REJECTED는 상대방만, WITHDRAWN은 보낸 당사자만
	// -----------------------------------------------------------
	@Transactional
	public QuoteResponse updateQuoteStatus(Long quoteId, Long currentUserId, Quote.QuoteStatus status) {
		Quote quote = findQuoteOrThrow(quoteId);

		if (status == Quote.QuoteStatus.WITHDRAWN) {
			quote.validateSenderOnly(currentUserId);
		} else {
			// ACCEPTED, REJECTED
			quote.validateCounterpartyOnly(currentUserId);
		}

		quote.updateStatus(status);

		if (status == Quote.QuoteStatus.REJECTED || status == Quote.QuoteStatus.WITHDRAWN) {
			// 채팅 없이 만들어진 견적(standalone)은 chatRoomId가 없을 수 있으므로 null 체크 필수
			if (quote.getChatRoomId() != null) {
				chatService.closeChatRoom(quote.getChatRoomId());
			}
		}

		// TODO: status == ACCEPTED 인 경우 Order 생성 로직 연결 필요
		// (Order 도메인 합류 후: OrderService.createFromQuote(quote) 형태로 연동)

		return QuoteResponse.from(quote);
	}

	// -----------------------------------------------------------
	// 내부 헬퍼
	// -----------------------------------------------------------

	private Quote buildQuoteFromChatRoom(Long chatRoomId, Long buyerId, Long sellerId,
	                                     Quote.SenderRole senderRole, QuoteCreateRequest request) {
		long totalAmount = Quote.calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		return Quote.builder()
				.chatRoomId(chatRoomId)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.senderRole(senderRole)
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();
	}

	private Quote buildStandaloneQuote(Long buyerId, Long sellerId, Quote.SenderRole senderRole,
	                                   Integer quantity, Long unitPrice, Long deliveryFee) {
		long totalAmount = Quote.calculateTotalAmount(quantity, unitPrice, deliveryFee);

		return Quote.builder()
				.chatRoomId(null)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.senderRole(senderRole)
				.quantity(quantity)
				.unitPrice(unitPrice)
				.deliveryFee(deliveryFee)
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();
	}

	private Quote findQuoteOrThrow(Long quoteId) {
		return quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));
	}
}