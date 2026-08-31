package org.example.matcheat.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatRoomResponse;
import org.example.matcheat.domain.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat-rooms")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping
	public ResponseEntity<ChatRoomResponse> createChatRoom(
			@RequestBody ChatRoomCreateRequest request
	) {
		// [수정] 별도로 하드코딩하지 않고 resolveCurrentUserId()로 일원화.
		// 인증 붙으면 이 메서드 하나만 바꾸면 이 클래스의 모든 엔드포인트에 적용된다.
		Long currentUserId = resolveCurrentUserId();

		ChatRoomResponse response = chatService.createChatRoom(request, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{chatRoomId}")
	public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long chatRoomId) {
		Long currentUserId = resolveCurrentUserId();
		ChatRoomResponse response = chatService.getChatRoom(chatRoomId, currentUserId);
		return ResponseEntity.ok(response);
	}

	// -----------------------------------------------------------
	// 인증 붙기 전 임시 처리 — 교체 지점을 한 곳으로 모아둔다.
	// QuoteController.resolveCurrentUserId()와 동일한 패턴.
	// -----------------------------------------------------------
	private Long resolveCurrentUserId() {
		// TODO: SecurityContext/JWT 적용 시 인증된 사용자의 userId로 교체
		return 1L;
	}
}