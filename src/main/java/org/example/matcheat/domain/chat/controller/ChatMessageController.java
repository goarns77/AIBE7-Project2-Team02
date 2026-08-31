package org.example.matcheat.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Chat Message API", description = "채팅 메시지 전송 및 이전 대화 내역 조회 API")
@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final SimpMessageSendingOperations messagingTemplate;
	private final ChatMessageService chatMessageService;

	/**
	 * [WebSocket] Client에서 /pub/chat/message 로 전송했을 때 처리
	 * [수정] request.getSenderId()를 신뢰하지 않고 resolveCurrentUserId()로 결정한다.
	 */
	@MessageMapping("/chat/message")
	public void sendMessage(ChatMessageCreateRequest request) {
		Long currentUserId = resolveCurrentUserId();

		ChatMessageResponse savedResponse = chatMessageService.saveMessage(request, currentUserId);

		messagingTemplate.convertAndSend(
				"/sub/chat/room/" + savedResponse.getChatRoomId(),
				savedResponse
		);
	}

	/**
	 * [HTTP API] 채팅방 입장 시 이전 대화 목록 조회
	 * [수정] 참여자 검증을 위해 currentUserId 전달
	 */
	@Operation(summary = "채팅방 이전 메시지 내역 조회", description = "특정 채팅방의 모든 메시지 내역을 조회합니다. 참여자만 조회할 수 있습니다.")
	@GetMapping("/api/v1/chat-rooms/{chatRoomId}/messages")
	public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long chatRoomId) {
		Long currentUserId = resolveCurrentUserId();
		List<ChatMessageResponse> history = chatMessageService.getChatHistory(chatRoomId, currentUserId);
		return ResponseEntity.ok(history);
	}

	// -----------------------------------------------------------
	// 인증 붙기 전 임시 처리 — 교체 지점을 한 곳으로 모아둔다.
	// TODO: STOMP CONNECT 시 JWT 검증하는 ChannelInterceptor가 붙으면
	// 이 메서드를 SimpMessageHeaderAccessor의 Principal에서 유도하도록 교체.
	// (HTTP 경로는 SecurityContext에서 유도하도록 교체 — QuoteController와 동일 패턴)
	// -----------------------------------------------------------
	private Long resolveCurrentUserId() {
		return 1L;
	}
}