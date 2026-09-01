package org.example.matcheat.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 결제 완료(COMPLETED) 시점에 자동 발행되는 정산서. 발행 이후 수정 메서드를
 * 의도적으로 두지 않았다 — "최종 확정 문서"라는 성격상 수정 API 자체가
 * 없는 게 맞다고 판단함. 나중에 플랫폼 수수료 등이 생기면 Payment.amount와
 * Settlement의 정산 금액이 달라질 수 있어 필드를 분리해뒀다(지금은 동일).
 */
@Entity
@Table(name = "settlements")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long paymentId;

	@Column(nullable = false)
	private Long quoteId;

	@Column(nullable = false)
	private Long buyerId;

	@Column(nullable = false)
	private Long sellerId;

	private Integer quantity;
	private Long unitPrice;
	private Long deliveryFee;
	private Long totalAmount;

	@Column(columnDefinition = "TEXT")
	private String additionalNotes;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime issuedAt;

	@Builder
	public Settlement(Long paymentId, Long quoteId, Long buyerId, Long sellerId,
	                  Integer quantity, Long unitPrice, Long deliveryFee, Long totalAmount,
	                  String additionalNotes) {
		this.paymentId = paymentId;
		this.quoteId = quoteId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.deliveryFee = deliveryFee;
		this.totalAmount = totalAmount;
		this.additionalNotes = additionalNotes;
	}

	public boolean isParticipant(Long userId) {
		return userId != null && (userId.equals(buyerId) || userId.equals(sellerId));
	}

	public void validateParticipant(Long userId) {
		if (!isParticipant(userId)) {
			throw new IllegalArgumentException("해당 정산서에 접근 권한이 없습니다.");
		}
	}
}