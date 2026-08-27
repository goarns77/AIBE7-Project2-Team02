package org.example.matcheat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatFile;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFileResponse {

	private Long id;
	private Long chatRoomId;
	private Long senderId;
	private String originalFileName;
	private String storedFileName;
	private Long fileSize;
	private String fileType;
	private LocalDateTime createdAt;

	// 단일 인자 from 메서드 (uploaderId는 null 처리)
	public static ChatFileResponse from(ChatFile chatFile) {
		return from(chatFile, null);
	}

	// uploaderId를 받는 오버로딩 from 메서드
	public static ChatFileResponse from(ChatFile chatFile, Long uploaderId) {
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