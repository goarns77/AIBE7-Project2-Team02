package org.example.matcheat.domain.quote.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "quotes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long chatRoomId; // nullable (채팅 없이 견적만 발행 가능)

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuoteStatus status;

	private LocalDateTime createdAt;

	// 💡 WITHDRAWN 추가
	public enum QuoteStatus {
		SENT, ACCEPTED, REJECTED, WITHDRAWN
	}

	@Builder
	public Quote(Long chatRoomId, Long buyerId, Long sellerId, Integer quantity, Long unitPrice, Long deliveryFee, Long totalAmount, QuoteStatus status) {
		this.chatRoomId = chatRoomId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.deliveryFee = deliveryFee;
		this.totalAmount = totalAmount;
		this.status = status != null ? status : QuoteStatus.SENT;
		this.createdAt = LocalDateTime.now();
	}

	public void updateChatRoomId(Long chatRoomId) {
		this.chatRoomId = chatRoomId;
	}

	public void updateStatus(QuoteStatus status) {
		this.status = status;
	}

	public void updateQuoteDetails(Integer quantity, Long unitPrice, Long deliveryFee, Long totalAmount) {
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.deliveryFee = deliveryFee;
		this.totalAmount = totalAmount;
	}
}