package org.example.matcheat.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.matcheat.domain.chat.entity.ChatMessage;

/**
 * [수정] senderId 필드 제거.
 * 메시지를 보낸 사람은 클라이언트가 주장하는 값이 아니라 서버(컨트롤러의
 * 인증 Principal이 결정한다. STOMP 세션의 Principal에서
 * 유도하도록 그 함수 내부만 바꾸면 된다.
 */
@Getter
@NoArgsConstructor
public class ChatMessageCreateRequest {
	private Long chatRoomId;
	private ChatMessage.MessageType messageType; // TEXT, IMAGE, PDF
	private String message;          // 텍스트 내용 또는 파일 URL/다운로드 링크
	private Long fileId;
	private String originalFileName; // 파일 원본 이름 (선택)
	private Long fileSize;           // 파일 크기 (선택)
}
