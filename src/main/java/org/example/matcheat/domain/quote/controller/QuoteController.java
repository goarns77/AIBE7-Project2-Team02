package org.example.matcheat.domain.quote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.quote.dto.QuoteResponse;
import org.example.matcheat.domain.quote.dto.QuoteUpdateRequest;
import org.example.matcheat.domain.quote.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quote API", description = "견적서 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuoteController {

	private final QuoteService quoteService;

	@Operation(summary = "1차 견적서 수동 생성 (테스트용)", description = "proposalId 기반으로 1차 견적서를 단독 생성합니다.")
	@PostMapping("/quotes/primary")
	public ResponseEntity<QuoteResponse> createPrimaryQuote(
			@RequestParam Long chatRoomId,
			@RequestParam Long proposalId) {
		Long currentUserId = 1L; // 테스트용 임시 로그인 유저 ID
		QuoteResponse response = quoteService.createPrimaryQuoteFromProposal(chatRoomId, proposalId, currentUserId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 수락", description = "구매자가 전달받은 견적서를 수락합니다.")
	@PatchMapping("/quotes/{quoteId}/accept")
	public ResponseEntity<QuoteResponse> acceptQuote(@PathVariable Long quoteId) {
		QuoteResponse response = quoteService.acceptQuote(quoteId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "견적서 거절", description = "구매자가 전달받은 견적서를 거절합니다.")
	@PatchMapping("/quotes/{quoteId}/reject")
	public ResponseEntity<QuoteResponse> rejectQuote(@PathVariable Long quoteId) {
		QuoteResponse response = quoteService.rejectQuote(quoteId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "채팅방 내 견적서 목록 조회", description = "해당 채팅방의 모든 견적서를 최신순으로 조회합니다.")
	@GetMapping("/chat-rooms/{chatRoomId}/quotes")
	public ResponseEntity<List<QuoteResponse>> getQuotesByChatRoom(@PathVariable Long chatRoomId) {
		List<QuoteResponse> quotes = quoteService.getQuotesByChatRoom(chatRoomId);
		return ResponseEntity.ok(quotes);
	}

	@Operation(summary = "견적서 수정 (재제안)", description = "구매자 또는 판매자가 수량, 단가, 배송비를 수정하여 새로운 견적서를 보냅니다.")
	@PostMapping("/chat-rooms/{chatRoomId}/quotes")
	public ResponseEntity<QuoteResponse> updateQuote(
			@PathVariable Long chatRoomId,
			@RequestBody QuoteUpdateRequest request) {

		Long currentUserId = 1L; // 테스트용 임시 사용자 ID
		QuoteResponse response = quoteService.updateQuote(chatRoomId, currentUserId, request);
		return ResponseEntity.ok(response);
	}
}