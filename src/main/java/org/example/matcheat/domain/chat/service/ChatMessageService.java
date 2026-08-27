package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageCreateRequest;
import org.example.matcheat.domain.chat.dto.ChatMessageResponse;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 메시지 저장 (ChatMessageCreateRequest 받음)
	 */
	@Transactional
	public ChatMessageResponse saveMessage(ChatMessageCreateRequest request) { // 파라미터 타입 변경
		ChatMessage chatMessage = ChatMessage.builder()
				.chatRoomId(request.getChatRoomId())
				.senderId(request.getSenderId())
				.messageType(request.getMessageType())
				.content(request.getMessage())
				.originalFileName(request.getOriginalFileName())
				.fileSize(request.getFileSize())
				.build();

		ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
		return ChatMessageResponse.from(savedMessage);
	}

	/**
	 * 이전 대화 내역 조회
	 */
	public List<ChatMessageResponse> getChatHistory(Long chatRoomId) {
		return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
				.stream()
				.map(ChatMessageResponse::from)
				.toList();
	}
}