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
	 */
	@MessageMapping("/chat/message")
	public void sendMessage(ChatMessageCreateRequest request) { // ChatMessageResponse -> ChatMessageCreateRequest 변경
		// 1. 메시지 DB 저장 (Request를 받아 저장 후 Response DTO 반환)
		ChatMessageResponse savedResponse = chatMessageService.saveMessage(request);

		// 2. 해당 채팅방 구독자들(/sub/chat/room/{chatRoomId})에게 메시지 전송
		messagingTemplate.convertAndSend(
				"/sub/chat/room/" + savedResponse.getChatRoomId(),
				savedResponse
		);
	}

	/**
	 * [HTTP API] 채팅방 입장 시 이전 대화 목록 조회
	 */
	@Operation(summary = "채팅방 이전 메시지 내역 조회", description = "특정 채팅방의 모든 메시지 내역을 조회합니다.")
	@GetMapping("/api/v1/chat-rooms/{chatRoomId}/messages")
	public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long chatRoomId) {
		List<ChatMessageResponse> history = chatMessageService.getChatHistory(chatRoomId);
		return ResponseEntity.ok(history);
	}
}