package org.example.matcheat.domain.quote.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.quote.dto.QuoteResponse;
import org.example.matcheat.domain.quote.dto.QuoteUpdateRequest;
import org.example.matcheat.domain.quote.entity.Quote;
import org.example.matcheat.domain.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteService {

	private final QuoteRepository quoteRepository;

	/**
	 * ChatService에서 호출하는 1차 견적서 자동 생성 메서드
	 */
	@Transactional
	public QuoteResponse createPrimaryQuoteFromProposal(Long chatRoomId, Long proposalId, Long buyerId) {

		// TODO: [팀원 Proposal 작업 완료 후 수정]
		// 현재 Proposal 도메인 개발 중이므로 임시 하드코딩 데이터 사용.
		// 추후 ProposalRepository.findById(proposalId)로 실제 수량, 단가, 판매자 ID를 조회하도록 변경해야 함.
		Long sellerId = 2L;          // 임시 판매자 ID
		int requestedQuantity = 10;  // 임시 요청 수량
		long unitPrice = 5000L;      // 임시 제안 단가
		long deliveryFee = 3000L;    // 임시 배송비
		long totalAmount = (requestedQuantity * unitPrice) + deliveryFee;

		Quote primaryQuote = Quote.builder()
				.chatRoomId(chatRoomId)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.quantity(requestedQuantity)
				.unitPrice(unitPrice)
				.deliveryFee(deliveryFee)
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(primaryQuote);
		return QuoteResponse.from(savedQuote);
	}

	@Transactional
	public QuoteResponse acceptQuote(Long quoteId) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 견적서입니다. ID: " + quoteId));

		quote.updateStatus(Quote.QuoteStatus.ACCEPTED);
		return QuoteResponse.from(quote);
	}

	@Transactional
	public QuoteResponse rejectQuote(Long quoteId) {
		Quote quote = quoteRepository.findById(quoteId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 견적서입니다. ID: " + quoteId));

		quote.updateStatus(Quote.QuoteStatus.REJECTED);
		return QuoteResponse.from(quote);
	}

	@Transactional(readOnly = true)
	public List<QuoteResponse> getQuotesByChatRoom(Long chatRoomId) {
		return quoteRepository.findByChatRoomIdOrderByIdDesc(chatRoomId)
				.stream()
				.map(QuoteResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 견적서 수정 (재제안)
	 * 기존 견적 이력을 보존하기 위해 새로운 Quote 엔티티를 생성합니다.
	 */
	@Transactional
	public QuoteResponse updateQuote(Long chatRoomId, Long currentUserId, QuoteUpdateRequest request) {
		// 총액 재계산
		long totalAmount = (request.getQuantity() * request.getUnitPrice()) + request.getDeliveryFee();

		// TODO: currentUserId 기반으로 구매자/판매자 ID 세팅 및 권한 검증 로직 추가 필요
		Long buyerId = 1L;  // 임시 구매자 ID
		Long sellerId = 2L; // 임시 판매자 ID

		Quote newQuote = Quote.builder()
				.chatRoomId(chatRoomId)
				.buyerId(buyerId)
				.sellerId(sellerId)
				.quantity(request.getQuantity())
				.unitPrice(request.getUnitPrice())
				.deliveryFee(request.getDeliveryFee())
				.totalAmount(totalAmount)
				.status(Quote.QuoteStatus.SENT)
				.build();

		Quote savedQuote = quoteRepository.save(newQuote);
		return QuoteResponse.from(savedQuote);
	}
}