// domain/chat/dto/ChatFileResponse.java
package org.example.matcheat.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatFileResponse {

	private Long id;
	private Long chatRoomId;
	private Long senderId;
	private String originalFileName;
	private String storedFileName;
	// [수정 P1-5] filePath 제거 (서버 절대경로 보안 노출 방지)
	private Long fileSize;
	private ChatMessage.MessageType fileType;
	private LocalDateTime createdAt;

	public static ChatFileResponse from(ChatFile chatFile) {
		return ChatFileResponse.builder()
				.id(chatFile.getId())
				.chatRoomId(chatFile.getChatRoomId())
				.senderId(chatFile.getSenderId())
				.originalFileName(chatFile.getOriginalFileName())
				.storedFileName(chatFile.getStoredFileName())
				.fileSize(chatFile.getFileSize())
				.fileType(chatFile.getFileType())
				.createdAt(chatFile.getCreatedAt())
				.build();
	}
}