package org.example.matcheat.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.payment.dto.ReceiptResponse;
import org.example.matcheat.domain.payment.dto.SettlementResponse;
import org.example.matcheat.domain.payment.entity.Payment;
import org.example.matcheat.domain.payment.service.PaymentService;
import org.example.matcheat.domain.payment.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment Document API", description = "영수증 / 정산서 조회")
@RestController
@RequestMapping("/api/v1/payments/{paymentId}")
@RequiredArgsConstructor
public class PaymentDocumentController {

	private final PaymentService paymentService;
	private final SettlementService settlementService;

	@Operation(summary = "결제 영수증 조회", description = "구매자 화면용. 결제 완료 건만 조회 가능.")
	@GetMapping("/receipt")
	public ResponseEntity<ReceiptResponse> getReceipt(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long paymentId) {
		Payment payment = paymentService.getPaymentEntity(paymentId, currentUserId(jwt));
		return ResponseEntity.ok(ReceiptResponse.from(payment));
	}

	@Operation(summary = "정산서 조회", description = "판매자 화면용. 결제 완료 시 자동 발행된 정산서를 조회한다.")
	@GetMapping("/settlement")
	public ResponseEntity<SettlementResponse> getSettlement(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long paymentId) {
		return ResponseEntity.ok(SettlementResponse.from(
				settlementService.getByPaymentId(paymentId, currentUserId(jwt))));
	}

	private static long currentUserId(Jwt jwt) {
		return Long.parseLong(jwt.getSubject());
	}
}
