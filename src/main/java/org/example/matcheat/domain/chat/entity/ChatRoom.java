package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long proposalId; // nullable (INQUIRY인 경우 null 가능)

	private Long quoteId;    // nullable (견적서 생성 시 업데이트)

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OriginType originType; // PROPOSAL | INQUIRY

	// 1. 상태(Status) 필드 추가 (기본값 ACTIVE)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatRoomStatus status;

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	private LocalDateTime createdAt;

	public enum OriginType {
		PROPOSAL, INQUIRY
	}

	// 2. 채팅방 상태 Enum 추가
	public enum ChatRoomStatus {
		ACTIVE, CLOSED
	}

	@Builder
	public ChatRoom(Long proposalId, Long quoteId, OriginType originType, ChatRoomStatus status, Long buyerId, Long sellerId) {
		this.proposalId = proposalId;
		this.quoteId = quoteId;
		this.originType = originType;
		// 3. 빌더에서 status가 지정되지 않으면 기본값 ACTIVE로 설정
		this.status = (status != null) ? status : ChatRoomStatus.ACTIVE;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.createdAt = LocalDateTime.now();
	}

	// 견적서 연동 메서드
	public void updateQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	// 4. 채팅방 종료 처리 메서드 추가
	public void close() {
		this.status = ChatRoomStatus.CLOSED;
	}
}