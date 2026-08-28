package org.example.matcheat.domain.quote.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.service.ChatService;
import org.example.matcheat.domain.quote.dto.QuoteCreateRequest;
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

	/**
	 * 액션 1: "견적서 보내기"
	 */
	@Transactional
	public QuoteResponse createQuoteWithNewChatRoom(Long currentUserId, Long sellerId, QuoteCreateRequest request) {
		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		// ChatService를 통해 1:1 PROPOSAL 방 생성/확보
		ChatRoom chatRoom = chatService.getOrCreateChatRoomForQuote(null, ChatRoom.OriginType.PROPOSAL, currentUserId, sellerId);

		// senderRole 판단
		Quote.SenderRole senderRole = currentUserId.equals(chatRoom.getBuyerId()) ? Quote.SenderRole.BUYER : Quote.SenderRole.SELLER;

		Quote quote = Quote.builder()
				.chatRoomId(chatRoom.getId())
				.buyerId(chatRoom.getBuyerId())
				.sellerId(chatRoom.getSellerId())
				.senderRole(senderRole)
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(quote);
		chatService.updateChatRoomQuoteId(chatRoom.getId(), savedQuote.getId());

		return QuoteResponse.from(savedQuote);
	}

	/**
	 * 액션 2: "대화 중 견적서 생성"
	 */
	@Transactional
	public QuoteResponse createQuoteInChatRoom(Long chatRoomId, Long currentUserId, QuoteCreateRequest request) {
		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		Quote quote = Quote.builder()
				.chatRoomId(chatRoomId)
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(quote);
		ChatRoom updatedChatRoom = chatService.updateChatRoomQuoteId(chatRoomId, savedQuote.getId());

		Quote.SenderRole senderRole = currentUserId.equals(updatedChatRoom.getBuyerId()) ? Quote.SenderRole.BUYER : Quote.SenderRole.SELLER;
		savedQuote.updateSenderRoleAndUsers(updatedChatRoom.getBuyerId(), updatedChatRoom.getSellerId(), senderRole);

		return QuoteResponse.from(savedQuote);
	}

	@Transactional(readOnly = true)
	public QuoteResponse getQuote(Long quoteId) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		return QuoteResponse.from(quote);
	}

	@Transactional
	public QuoteResponse updateQuoteStatus(Long quoteId, Quote.QuoteStatus status) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		// P0-3 엔티티 내 가드 검증 수행
		quote.updateStatus(status);

		if (status == Quote.QuoteStatus.REJECTED || status == Quote.QuoteStatus.WITHDRAWN) {
			if (quote.getChatRoomId() != null) {
				chatService.closeChatRoom(quote.getChatRoomId());
			}
		}

		return QuoteResponse.from(quote);
	}

	@Transactional
	public QuoteResponse updateQuote(Long quoteId, QuoteUpdateRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		// P0-3 엔티티 내 가드 검증 수행
		quote.updateQuoteDetails(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee(), totalAmount);

		return QuoteResponse.from(quote);
	}

	private long calculateTotalAmount(Integer quantity, Long unitPrice, Long deliveryFee) {
		long qty = (quantity != null) ? quantity : 0;
		long price = (unitPrice != null) ? unitPrice : 0L;
		long fee = (deliveryFee != null) ? deliveryFee : 0L;
		return (qty * price) + fee;
	}
}