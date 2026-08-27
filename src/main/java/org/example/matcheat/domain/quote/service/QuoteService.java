package org.example.matcheat.domain.quote.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatRoom;
import org.example.matcheat.domain.chat.repository.ChatRoomRepository;
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
	private final ChatRoomRepository chatRoomRepository;

	/**
	 * 액션 1: "견적서 보내기"
	 * 견적서를 우선 생성하고, 이에 연결되는 1:1 채팅방(OriginType.PROPOSAL)을 자동 생성하여 상호 연결합니다.
	 */
	@Transactional
	public QuoteResponse createQuoteWithNewChatRoom(Long currentUserId, Long sellerId, QuoteCreateRequest request) {
		// 1. 견적 금액 계산
		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		// 2. ChatRoom을 먼저 생성 및 저장 (quoteId는 일단 null)
		ChatRoom chatRoom = ChatRoom.builder()
				.originType(ChatRoom.OriginType.PROPOSAL)
				.quoteId(null) // 아래에서 Quote 생성 후 업데이트
				.buyerId(currentUserId)
				.sellerId(sellerId)
				.build();

		ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

		// 3. 확보된 chatRoomId를 넣어 Quote 생성 및 저장
		Quote quote = Quote.builder()
				.chatRoomId(savedChatRoom.getId()) // 👈 NULL이 아닌 채로 세팅되어 NOT NULL 제약조건 통과
				.buyerId(currentUserId)
				.sellerId(sellerId)
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(quote);

		// 4. ChatRoom에 생성된 quoteId 역방향 연결
		savedChatRoom.updateQuoteId(savedQuote.getId());

		return QuoteResponse.from(savedQuote);
	}

	/**
	 * 액션 2: "대화 중 견적서 생성"
	 * 기존 채팅방 내에서 견적서를 작성하여 발행하고, 해당 채팅방의 최신 quote_id를 업데이트합니다.
	 */
	@Transactional
	public QuoteResponse createQuoteInChatRoom(Long chatRoomId, Long currentUserId, QuoteCreateRequest request) {
		// 1. 채팅방 존재 여부 확인
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
				.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + chatRoomId));

		// 2. 견적 금액 계산
		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		// 3. 견적서(Quote) 생성 (chatRoomId 세팅)
		Quote quote = Quote.builder()
				.chatRoomId(chatRoom.getId())
				.buyerId(chatRoom.getBuyerId())
				.sellerId(chatRoom.getSellerId())
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(quote);

		// 4. 채팅방의 최신 quote_id 업데이트
		chatRoom.updateQuoteId(savedQuote.getId());

		return QuoteResponse.from(savedQuote);
	}

	/**
	 * 견적서 단건 조회
	 */
	@Transactional(readOnly = true)
	public QuoteResponse getQuote(Long quoteId) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		return QuoteResponse.from(quote);
	}

	/**
	 * 견적 수락/거절 등 상태 변경
	 */
	@Transactional
	public QuoteResponse updateQuoteStatus(Long quoteId, Quote.QuoteStatus status) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		// 1. 견적서 상태 업데이트
		quote.updateStatus(status);

		// 2. 견적 상태가 REJECTED(거절) 또는 WITHDRAWN(철회)인 경우 채팅방을 CLOSED 상태로 변경
		if (status == Quote.QuoteStatus.REJECTED || status == Quote.QuoteStatus.WITHDRAWN) {
			if (quote.getChatRoomId() != null) {
				ChatRoom chatRoom = chatRoomRepository.findById(quote.getChatRoomId())
						.orElseThrow(() -> new IllegalArgumentException("연결된 채팅방을 찾을 수 없습니다. ID: " + quote.getChatRoomId()));

				// 채팅방 상태를 CLOSED로 변경 (Dirty Checking으로 자동 DB 반영)
				chatRoom.close();
			}
		}

		return QuoteResponse.from(quote);
	}

	// 총 금액 계산 도우미 메서드
	private long calculateTotalAmount(Integer quantity, Long unitPrice, Long deliveryFee) {
		long qty = (quantity != null) ? quantity : 0;
		long price = (unitPrice != null) ? unitPrice : 0L;
		long fee = (deliveryFee != null) ? deliveryFee : 0L;
		return (qty * price) + fee;
	}

	/**
	 * 견적서 금액 및 수량 수정
	 */
	@Transactional
	public QuoteResponse updateQuote(Long quoteId, QuoteUpdateRequest request) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다. ID: " + quoteId));

		long totalAmount = calculateTotalAmount(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee());

		// Quote 엔티티 내부 update 메서드 활용
		quote.updateQuoteDetails(request.getQuantity(), request.getUnitPrice(), request.getDeliveryFee(), totalAmount);

		return QuoteResponse.from(quote);
	}

}