package org.example.matcheat.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatRoomCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatRoomResponse;
import org.example.matcheat.domain.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	/**
	 * 채팅방 생성 API (단순 문의 및 제안서 기반 생성 공통)
	 * POST /api/chat-rooms
	 */
	@PostMapping
	public ResponseEntity<ChatRoomResponse> createChatRoom(
			@RequestBody ChatRoomCreateRequest request
			// TODO: 추후 Spring Security 적용 시 @AuthenticationPrincipal 사용
			// @AuthenticationPrincipal CustomUserDetails userDetails
	) {
		// 인증 구현 전 임시 사용자 ID 하드코딩 (예: 구매자 ID = 1L)
		Long currentUserId = 1L;

		ChatRoomResponse response = chatService.createChatRoom(request, currentUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 단건 채팅방 상세 조회 API
	 * GET /api/chat-rooms/{chatRoomId}
	 */
	@GetMapping("/{chatRoomId}")
	public ResponseEntity<ChatRoomResponse> getChatRoom(@PathVariable Long chatRoomId) {
		ChatRoomResponse response = chatService.getChatRoom(chatRoomId);
		return ResponseEntity.ok(response);
	}
}