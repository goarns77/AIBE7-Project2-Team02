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
	private String filePath;
	private Long fileSize;

	// [P1-5 수정] String -> ChatMessage.MessageType Enum으로 변경
	private ChatMessage.MessageType fileType;

	private LocalDateTime createdAt;

	public static ChatFileResponse from(ChatFile chatFile) {
		return ChatFileResponse.builder()
				.id(chatFile.getId())
				.chatRoomId(chatFile.getChatRoomId())
				.senderId(chatFile.getSenderId())
				.originalFileName(chatFile.getOriginalFileName())
				.storedFileName(chatFile.getStoredFileName())
				.filePath(chatFile.getFilePath())
				.fileSize(chatFile.getFileSize())
				.fileType(chatFile.getFileType()) // Enum 타입 매핑
				.createdAt(chatFile.getCreatedAt())
				.build();
	}
}