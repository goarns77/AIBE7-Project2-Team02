package org.example.matcheat.domain.chat.controller;

import org.example.matcheat.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	// WBS 7.2: 거래 협의 채팅방 생성
	@PostMapping("/proposals/{proposalId}/chat-room")
	public ResponseEntity<Map<String, Object>> createChatRoom(
			@PathVariable Long proposalId
			// @AuthenticationPrincipal CustomUserDetails userDetails // JWT 인증 연동 시 사용
	) {
		Long currentUserId = 1L; // 임시 인증 ID
		Long chatRoomId = chatService.createChatRoom(proposalId, currentUserId);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"chatRoomId", chatRoomId,
				"proposalStatus", "IN_TALK",
				"requestStatus", "IN_TALK"
		));
	}
}