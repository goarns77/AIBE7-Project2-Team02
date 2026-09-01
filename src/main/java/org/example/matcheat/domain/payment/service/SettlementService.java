package org.example.matcheat.domain.payment.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.payment.entity.Payment;
import org.example.matcheat.domain.payment.entity.Settlement;
import org.example.matcheat.domain.payment.repository.SettlementRepository;
import org.example.matcheat.domain.quote.entity.Quote;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private final SettlementRepository settlementRepository;
	private final PaymentAccessService paymentAccess;

	@Transactional
	public Settlement issueSettlement(Payment payment, Quote quote) {
		// 멱등 처리 — 이미 발행되어 있으면 다시 만들지 않는다.
		return settlementRepository.findByPaymentId(payment.getId())
				.orElseGet(() -> settlementRepository.save(Settlement.builder()
						.paymentId(payment.getId())
						.quoteId(quote.getId())
						.buyerId(quote.getBuyerId())
						.sellerId(quote.getSellerId())
						.quantity(quote.getQuantity())
						.unitPrice(quote.getUnitPrice())
						.deliveryFee(quote.getDeliveryFee())
						.totalAmount(quote.getTotalAmount())
						.additionalNotes(quote.getAdditionalNotes())
						.build()));
	}

	@Transactional(readOnly = true)
	public Settlement getByPaymentId(Long paymentId, Long currentUserId) {
		Settlement settlement = settlementRepository.findByPaymentId(paymentId)
				.orElseThrow(() -> new IllegalArgumentException("정산서를 찾을 수 없습니다. paymentId: " + paymentId));
		paymentAccess.requireSettlementSeller(settlement, currentUserId);
		return settlement;
	}
}
