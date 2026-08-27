package org.example.matcheat.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatMessage;

@Getter
@NoArgsConstructor
public class ChatMessageCreateRequest {
	private Long chatRoomId;
	private Long senderId;
	private ChatMessage.MessageType messageType; // TEXT, IMAGE, PDF
	private String message;          // 텍스트 내용 또는 파일 URL/다운로드 링크
	private String originalFileName; // 파일 원본 이름 (선택)
	private Long fileSize;           // 파일 크기 (선택)
}