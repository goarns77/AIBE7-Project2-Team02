// domain/quote/controller/QuoteController.java
package org.example.matcheat.domain.quote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.quote.dto.QuoteCreateRequest;
import org.example.matcheat.domain.quote.dto.QuoteDirectRequestToBuyer;
import org.example.matcheat.domain.quote.dto.QuoteDirectRequestToSeller;
import org.example.matcheat.domain.quote.dto.QuoteResponse;
import org.example.matcheat.domain.quote.dto.QuoteStatusUpdateRequest;
import org.example.matcheat.domain.quote.dto.QuoteUpdateRequest;
import org.example.matcheat.domain.quote.service.QuoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quote API", description = "견적서 생성, 조회, 수정, 상태 변경 API")
@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

	private final QuoteService quoteService;

	@Operation(summary = "채팅방 자동 생성 + 견적서 생성", description = "구매자가 판매자를 지정해 견적을 요청하면 1:1 채팅방(PROPOSAL)을 자동 생성하고 견적서를 만듭니다.")
	@PostMapping("/direct")
	public ResponseEntity<QuoteResponse> createQuoteWithNewChatRoom(
			@RequestParam Long sellerId,
			@Valid @RequestBody QuoteCreateRequest request) {

		Long currentBuyerId = resolveCurrentUserId();
		QuoteResponse response = quoteService.createQuoteWithNewChatRoom(currentBuyerId, sellerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "채팅방 내 견적서 생성", description = "기존 채팅방(INQUIRY 또는 PROPOSAL)에서 견적서를 생성해 공유합니다. 요청자가 해당 채팅방의 참여자가 아니면 거부됩니다.")
	@PostMapping("/chat-rooms/{chatRoomId}")
	public ResponseEntity<QuoteResponse> createQuoteInChatRoom(
			@PathVariable Long chatRoomId,
			@Valid @RequestBody QuoteCreateRequest request) {

		Long currentUserId = resolveCurrentUserId();
		QuoteResponse response = quoteService.createQuoteInChatRoom(chatRoomId, currentUserId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "[판매자 → 구매자] 채팅 없이 독립 견적서 생성", description = "판매자가 채팅방 없이 특정 구매자에게 바로 견적서를 발송합니다.")
	@PostMapping("/to-buyer")
	public ResponseEntity<QuoteResponse> createStandaloneQuoteToBuyer(
			@Valid @RequestBody QuoteDirectRequestToBuyer request) {

		Long currentSellerId = resolveCurrentSellerId();
		QuoteResponse response = quoteService.createStandaloneQuoteToBuyer(currentSellerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "[구매자 → 판매자] 채팅 없이 독립 견적서 생성", description = "구매자가 채팅방 없이 특정 판매자에게 바로 견적을 제시/요청합니다.")
	@PostMapping("/to-seller")
	public ResponseEntity<QuoteResponse> createStandaloneQuoteToSeller(
			@Valid @RequestBody QuoteDirectRequestToSeller request) {

		Long currentBuyerId = resolveCurrentUserId();
		QuoteResponse response = quoteService.createStandaloneQuoteToSeller(currentBuyerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "견적서 단건 조회", description = "quoteId로 견적서 상세 정보를 조회합니다. 참여자(구매자/판매자)만 조회할 수 있습니다.")
	@GetMapping("/{quoteId}")
	public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long quoteId) {
		Long currentUserId = resolveCurrentUserId();
		QuoteResponse response = quoteService.getQuote(quoteId, currentUserId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 수량/금액 수정", description = "견적서를 보낸 당사자가 SENT 상태에서만 수정할 수 있습니다.")
	@PutMapping("/{quoteId}")
	public ResponseEntity<QuoteResponse> updateQuote(
			@PathVariable Long quoteId,
			@Valid @RequestBody QuoteUpdateRequest request) {

		Long currentUserId = resolveCurrentUserId();
		QuoteResponse response = quoteService.updateQuote(quoteId, currentUserId, request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 상태 변경", description = "ACCEPTED/REJECTED는 받은 상대방만, WITHDRAWN은 보낸 당사자만 수행할 수 있습니다.")
	@PatchMapping("/{quoteId}/status")
	public ResponseEntity<QuoteResponse> updateQuoteStatus(
			@PathVariable Long quoteId,
			@Valid @RequestBody QuoteStatusUpdateRequest request) {

		Long currentUserId = resolveCurrentUserId();
		QuoteResponse response = quoteService.updateQuoteStatus(quoteId, currentUserId, request.getStatus());
		return ResponseEntity.ok(response);
	}

	// -----------------------------------------------------------
	// 인증 붙기 전 임시 처리 — 교체 지점을 한 곳으로 모아둔다.
	// 인증 붙으면 이 두 메서드 내부만 SecurityContext 기반으로 바꾸면 된다.
	// -----------------------------------------------------------

	private Long resolveCurrentUserId() {
		// TODO: SecurityContext/JWT 적용 시 인증된 사용자의 userId로 교체
		return 1L;
	}

	private Long resolveCurrentSellerId() {
		// TODO: 인증 붙으면 SecurityContext의 userId로 seller_profiles를 조회해
		// sellerId를 서버가 결정해야 함 (합의안 3장). seller_profiles 도메인이
		// 아직 없어 임시로 userId를 그대로 사용.
		return 1L;
	}
}