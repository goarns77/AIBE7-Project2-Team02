package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "chat_room_id", nullable = false)
	private Long chatRoomId;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String message;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public ChatMessage(Long chatRoomId, Long senderId, String message) {
		this.chatRoomId = chatRoomId;
		this.senderId = senderId;
		this.message = message;
		this.createdAt = LocalDateTime.now();
	}
}