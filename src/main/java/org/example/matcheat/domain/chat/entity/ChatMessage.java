package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long chatRoomId;

	@Column(nullable = false)
	private Long senderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageType messageType; // TEXT, IMAGE, PDF

	@Column(columnDefinition = "TEXT")
	private String content; // TEXT는 일반 메시지, IMAGE/PDF는 파일 접근 URL

	private String originalFileName; // 파일 원본 이름 (PDF/이미지 다운로드 및 표시용)

	private Long fileSize; // 파일 용량 (bytes)

	private LocalDateTime createdAt;

	public enum MessageType {
		TEXT, IMAGE, PDF
	}

	@Builder
	public ChatMessage(Long chatRoomId, Long senderId, MessageType messageType,
	                   String content, String originalFileName, Long fileSize) {
		this.chatRoomId = chatRoomId;
		this.senderId = senderId;
		this.messageType = messageType != null ? messageType : MessageType.TEXT;
		this.content = content;
		this.originalFileName = originalFileName;
		this.fileSize = fileSize;
		this.createdAt = LocalDateTime.now();
	}
}