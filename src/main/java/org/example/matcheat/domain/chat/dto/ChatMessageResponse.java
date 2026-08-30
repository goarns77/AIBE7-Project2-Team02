package org.example.matcheat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatFile;
import org.example.matcheat.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

	private Long id;                  // 메시지 PK
	private Long chatRoomId;
	private Long senderId;
	private ChatMessage.MessageType messageType; // TEXT, IMAGE, PDF
	private String message;           // 텍스트 내용 또는 파일 URL/다운로드 링크
	private String originalFileName;  // 원본 파일명
	private Long fileSize;            // 파일 크기
	private LocalDateTime createdAt;

	public static ChatMessageResponse from(ChatMessage entity) {
		String downloadUrl = entity.getContent();
		String originalFileName = null;
		Long fileSize = null;

		// ChatFile 연관 객체가 존재하면 파일 관련 정보 매핑
		if (entity.getChatFile() != null) {
			ChatFile file = entity.getChatFile();
			downloadUrl = "/api/v1/chat-files/" + file.getId() + "/download";
			originalFileName = file.getOriginalFileName();
			fileSize = file.getFileSize();
		}

		return ChatMessageResponse.builder()
				.id(entity.getId())
				.chatRoomId(entity.getChatRoomId())
				.senderId(entity.getSenderId())
				.messageType(entity.getMessageType())
				.message(downloadUrl)
				.originalFileName(originalFileName)
				.fileSize(fileSize)
				.createdAt(entity.getCreatedAt())
				.build();
	}
}