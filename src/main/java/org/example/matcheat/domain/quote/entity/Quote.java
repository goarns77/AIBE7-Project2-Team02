package org.example.matcheat.domain.quote.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "quotes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long chatRoomId;

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private Long unitPrice;

	@Column(nullable = false)
	private Long deliveryFee;

	@Column(nullable = false)
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuoteStatus status;

	public enum QuoteStatus {
		SENT, ACCEPTED, REJECTED
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
	}

	public void updateStatus(QuoteStatus status) {
		this.status = status;
	}
}