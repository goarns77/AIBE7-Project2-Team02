package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long chatRoomId;

	@Column(nullable = false)
	private Long senderId;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageType messageType; // TEXT, IMAGE, PDF 등

	// chat_messages 테이블의 chat_file_id 컬럼을 FK로 연결
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "chat_file_id", nullable = true)
	private ChatFile chatFile;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public enum MessageType {
		TEXT, IMAGE, PDF
	}
}