package org.example.matcheat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
	private Long chatRoomId;
	private Long senderId;
	private String message;
	private LocalDateTime createdAt;

	public static ChatMessageDto from(org.example.matcheat.domain.chat.entity.ChatMessage entity) {
		return ChatMessageDto.builder()
				.chatRoomId(entity.getChatRoomId())
				.senderId(entity.getSenderId())
				.message(entity.getMessage())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}