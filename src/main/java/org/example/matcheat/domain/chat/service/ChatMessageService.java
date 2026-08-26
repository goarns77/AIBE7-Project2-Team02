package org.example.matcheat.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.chat.dto.ChatMessageDto;
import org.example.matcheat.domain.chat.entity.ChatMessage;
import org.example.matcheat.domain.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public ChatMessageDto saveMessage(ChatMessageDto dto) {
		ChatMessage chatMessage = ChatMessage.builder()
				.chatRoomId(dto.getChatRoomId())
				.senderId(dto.getSenderId())
				.message(dto.getMessage())
				.build();

		ChatMessage saved = chatMessageRepository.save(chatMessage);
		return ChatMessageDto.from(saved);
	}

	@Transactional(readOnly = true)
	public List<ChatMessageDto> getChatHistory(Long chatRoomId) {
		return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
				.stream()
				.map(ChatMessageDto::from)
				.collect(Collectors.toList());
	}
}