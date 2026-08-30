package org.example.matcheat.domain.quote.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long chatRoomId;
	private Long buyerId;
	private Long sellerId;

	// [P1-4 추가] 발신자 역할 (BUYER, SELLER)
	@Enumerated(EnumType.STRING)
	private SenderRole senderRole;

	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
	private QuoteStatus status;

	// [P1-4 추가] SenderRole Enum
	public enum SenderRole {
		BUYER,
		SELLER
	}

	public enum QuoteStatus {
		SENT,
		ACCEPTED,
		REJECTED,
		WITHDRAWN
	}

	@Builder
	public Quote(Long chatRoomId, Long buyerId, Long sellerId, SenderRole senderRole, Integer quantity, Long unitPrice, Long deliveryFee, Long totalAmount, QuoteStatus status) {
		this.chatRoomId = chatRoomId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.senderRole = senderRole;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.deliveryFee = deliveryFee;
		this.totalAmount = totalAmount;
		this.status = status != null ? status : QuoteStatus.SENT;
	}

	// [P0-3 추가] Quote 상태 전이 가드 (SENT 상태에서만 변경 가능)
	public void updateStatus(QuoteStatus newStatus) {
		if (this.status != QuoteStatus.SENT) {
			throw new IllegalStateException("이미 처리 완료된 견적서의 상태는 변경할 수 없습니다. 현재 상태: " + this.status);
		}
		if (newStatus == QuoteStatus.SENT) {
			throw new IllegalArgumentException("동일한 SENT 상태로 전이할 수 없습니다.");
		}
		this.status = newStatus;
	}

	// [P0-3 추가] 견적 내용 수정 가드 (SENT 상태일 때만 수정 허용)
	public void updateQuoteDetails(Integer quantity, Long unitPrice, Long deliveryFee, Long totalAmount) {
		if (this.status != QuoteStatus.SENT) {
			throw new IllegalStateException("SENT 상태의 견적서만 수정할 수 있습니다. 현재 상태: " + this.status);
		}
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.deliveryFee = deliveryFee;
		this.totalAmount = totalAmount;
	}

	// [P1-4 추가] createQuoteInChatRoom 지원용 업데이트 메서드
	public void updateSenderRoleAndUsers(Long buyerId, Long sellerId, SenderRole senderRole) {
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.senderRole = senderRole;
	}
}