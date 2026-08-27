package org.example.matcheat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
	private Long id;                  // 메시지 PK (추가 추천)
	private Long chatRoomId;
	private Long senderId;
	private ChatMessage.MessageType messageType; // TEXT, IMAGE, PDF 추가
	private String message;           // 텍스트 내용 또는 파일 URL/다운로드 링크
	private String originalFileName;  // 원본 파일명 (추가)
	private Long fileSize;            // 파일 크기 (추가)
	private LocalDateTime createdAt;

	public static ChatMessageResponse from(ChatMessage entity) {
		return ChatMessageResponse.builder()
				.id(entity.getId())
				.chatRoomId(entity.getChatRoomId())
				.senderId(entity.getSenderId())
				.messageType(entity.getMessageType())
				.message(entity.getContent()) // content -> message 매핑
				.originalFileName(entity.getOriginalFileName())
				.fileSize(entity.getFileSize())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}