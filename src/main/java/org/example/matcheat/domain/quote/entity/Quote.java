package org.example.matcheat.domain.quote.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long chatRoomId;
	private Long sellerId;
	private Long buyerId;

	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;

	@Enumerated(EnumType.STRING)
	private QuoteStatus status; // DRAFT, SENT, ACCEPTED, REJECTED

	public enum QuoteStatus {
		DRAFT, SENT, ACCEPTED, REJECTED
	}

	public void updateStatus(QuoteStatus status) {
		this.status = status;
	}
}