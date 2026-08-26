package org.example.matcheat.domain.quote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.quote.dto.QuoteCreateRequest;
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

	@Operation(summary = "독립 견적서 생성 (채팅방 자동 생성)", description = "견적서를 우선 작성하여 보내고, 1:1 채팅방(PROPOSAL)을 자동으로 생성합니다.")
	@PostMapping("/direct")
	public ResponseEntity<QuoteResponse> createQuoteWithNewChatRoom(
			@RequestParam Long sellerId,
			@RequestBody QuoteCreateRequest request) {

		Long currentUserId = 1L; // 인증 구현 전 임시 사용자 ID (구매자)
		QuoteResponse response = quoteService.createQuoteWithNewChatRoom(currentUserId, sellerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "채팅방 내 견적서 생성", description = "기존 채팅방(INQUIRY 또는 PROPOSAL)에서 새로운 견적서를 작성하여 공유합니다.")
	@PostMapping("/chat-rooms/{chatRoomId}")
	public ResponseEntity<QuoteResponse> createQuoteInChatRoom(
			@PathVariable Long chatRoomId,
			@RequestBody QuoteCreateRequest request) {

		Long currentUserId = 1L; // 인증 구현 전 임시 사용자 ID
		QuoteResponse response = quoteService.createQuoteInChatRoom(chatRoomId, currentUserId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "견적서 단건 조회", description = "quoteId를 이용하여 견적서 상세 정보를 조회합니다.")
	@GetMapping("/{quoteId}")
	public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long quoteId) {
		QuoteResponse response = quoteService.getQuote(quoteId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 수량/금액 수정", description = "판매자가 견적서의 수량, 단가, 배송비를 수정합니다.")
	@PutMapping("/{quoteId}")
	public ResponseEntity<QuoteResponse> updateQuote(
			@PathVariable Long quoteId,
			@RequestBody QuoteUpdateRequest request) {

		QuoteResponse response = quoteService.updateQuote(quoteId, request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 상태 변경 (수락/거절)", description = "구매자가 견적서를 수락(ACCEPTED)하거나 거절(REJECTED)합니다.")
	@PatchMapping("/{quoteId}/status")
	public ResponseEntity<QuoteResponse> updateQuoteStatus(
			@PathVariable Long quoteId,
			@RequestBody QuoteStatusUpdateRequest request) {

		QuoteResponse response = quoteService.updateQuoteStatus(quoteId, request.getStatus());
		return ResponseEntity.ok(response);
	}
}