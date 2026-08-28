package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long chatRoomId;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFileName;

	@Column(nullable = false)
	private String filePath;

	@Column(nullable = false)
	private Long fileSize;

	// [P1-5 수정] String -> ChatMessage.MessageType Enum 통합
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatMessage.MessageType fileType;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}