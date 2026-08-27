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

	@Column(name = "sender_id", nullable = false) // 👈 sender_id로 매핑 변경
	private Long senderId;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFileName;

	@Column(nullable = false)
	private String filePath;

	@Column(nullable = false)
	private Long fileSize;

	@Column(nullable = false)
	private String fileType; // IMAGE, PDF 등

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}