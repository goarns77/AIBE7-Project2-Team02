package org.example.matcheat.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class) // [추가] Auditing 기능 활성화
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long proposalId;
	private Long quoteId;

	@Enumerated(EnumType.STRING)
	private OriginType originType;

	private Long buyerId;
	private Long sellerId;

	@Enumerated(EnumType.STRING)
	private Status status = Status.ACTIVE;

	@CreatedDate // [추가] 생성 일시 자동 세팅
	@Column(updatable = false)
	private LocalDateTime createdAt;

	public enum OriginType {
		INQUIRY,
		PROPOSAL
	}

	public enum Status {
		ACTIVE,
		CLOSED
	}

	@Builder
	public ChatRoom(Long proposalId, Long quoteId, OriginType originType, Long buyerId, Long sellerId, Status status) {
		this.proposalId = proposalId;
		this.quoteId = quoteId;
		this.originType = originType;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.status = status != null ? status : Status.ACTIVE;
	}

	public void updateQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	public void close() {
		this.status = Status.CLOSED;
	}
}