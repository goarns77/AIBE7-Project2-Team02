package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long chatRoomId;

	@Column(nullable = false)
	private Long uploaderId;

	@Column(nullable = false)
	private String originalFileName; // 원본 파일명 (다운로드 시 사용)

	@Column(nullable = false)
	private String storedFileName;   // 서버 저장용 고유 파일명 (UUID 적용)

	@Column(nullable = false)
	private String filePath;         // 저장 경로

	@Column(nullable = false)
	private String fileType;         // IMAGE 또는 PDF

	@Column(nullable = false)
	private Long fileSize;           // 파일 크기 (Byte)

	private LocalDateTime createdAt;

	@Builder
	public ChatFile(Long chatRoomId, Long uploaderId, String originalFileName, String storedFileName, String filePath, String fileType, Long fileSize) {
		this.chatRoomId = chatRoomId;
		this.uploaderId = uploaderId;
		this.originalFileName = originalFileName;
		this.storedFileName = storedFileName;
		this.filePath = filePath;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.createdAt = LocalDateTime.now();
	}
}