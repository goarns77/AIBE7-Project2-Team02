package org.example.matcheat.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageDto;
import org.example.matcheat.domain.chat.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {

	private final SimpMessageSendingOperations messagingTemplate;
	private final ChatMessageService chatMessageService;

	/**
	 * [WebSocket] Client에서 /pub/chat/message 로 전송했을 때 처리
	 */
	@MessageMapping("/chat/message")
	public void sendMessage(ChatMessageDto messageDto) {
		// 1. 메시지 DB 저장
		ChatMessageDto savedDto = chatMessageService.saveMessage(messageDto);

		// 2. 해당 채팅방 구독자들(/sub/chat/room/{chatRoomId})에게 메시지 전송
		messagingTemplate.convertAndSend(
				"/sub/chat/room/" + savedDto.getChatRoomId(),
				savedDto
		);
	}

	/**
	 * [HTTP API] 채팅방 입장 시 이전 대화 목록 조회
	 */
	@GetMapping("/api/v1/chat-rooms/{chatRoomId}/messages")
	public ResponseEntity<List<ChatMessageDto>> getChatHistory(@PathVariable Long chatRoomId) {
		List<ChatMessageDto> history = chatMessageService.getChatHistory(chatRoomId);
		return ResponseEntity.ok(history);
	}
}