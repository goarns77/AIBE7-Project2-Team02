package org.example.matcheat.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.payment.dto.PaymentResponse;
import org.example.matcheat.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment API", description = "확정된 견적서 결제 (목업 PG)")
@RestController
@RequestMapping("/api/v1/quotes/{quoteId}/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@Operation(summary = "결제 실행", description = "ACCEPTED 상태의 견적서를 결제한다. 구매자 본인만 가능하며, 이미 완료된 결제가 있으면 그대로 반환한다(중복결제 방지).")
	@PostMapping
	public ResponseEntity<PaymentResponse> pay(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long quoteId) {
		return ResponseEntity.ok(paymentService.pay(quoteId, currentUserId(jwt)));
	}

	@Operation(summary = "견적서 기준 결제 조회")
	@GetMapping
	public ResponseEntity<PaymentResponse> get(
			@AuthenticationPrincipal Jwt jwt,
			@PathVariable Long quoteId) {
		return ResponseEntity.ok(paymentService.getByQuoteId(quoteId, currentUserId(jwt)));
	}

	private static long currentUserId(Jwt jwt) {
		return Long.parseLong(jwt.getSubject());
	}
}
