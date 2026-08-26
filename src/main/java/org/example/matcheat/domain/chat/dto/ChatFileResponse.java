package org.example.matcheat.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.matcheat.domain.chat.entity.ChatFile;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatFileResponse {
	private Long fileId;
	private Long chatRoomId;
	private Long uploaderId;
	private String originalFileName;
	private String fileType;
	private Long fileSize;
	private LocalDateTime createdAt;

	public static ChatFileResponse from(ChatFile chatFile) {
		return ChatFileResponse.builder()
				.fileId(chatFile.getId())
				.chatRoomId(chatFile.getChatRoomId())
				.uploaderId(chatFile.getUploaderId())
				.originalFileName(chatFile.getOriginalFileName())
				.fileType(chatFile.getFileType())
				.fileSize(chatFile.getFileSize())
				.createdAt(chatFile.getCreatedAt())
				.build();
	}
}