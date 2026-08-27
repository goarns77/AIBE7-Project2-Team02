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

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	private LocalDateTime createdAt;

	public enum OriginType {
		PROPOSAL, INQUIRY
	}

	@Builder
	public ChatRoom(Long proposalId, Long quoteId, OriginType originType, Long buyerId, Long sellerId) {
		this.proposalId = proposalId;
		this.quoteId = quoteId;
		this.originType = originType;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.createdAt = LocalDateTime.now();
	}

	// 견적서 연동 메서드
	public void updateQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}
}