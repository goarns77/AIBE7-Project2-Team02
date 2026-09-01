package org.example.matcheat.domain.matching.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.matching.dto.MatchingResultDTO;
import org.example.matcheat.domain.matching.service.MatchingService;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 구매자의 주문을 기준으로 계산된 상품 매칭 결과를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final OrderRequestService orderRequestService;

    /**
     * 현재 로그인한 구매자가 등록한 주문의 상품 매칭 결과를 조회한다.
     */
    @GetMapping("/{requestId}/matches")
    public ResponseEntity<List<MatchingResultDTO>> findMatches(
            @PathVariable Long requestId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long currentUserId =
                Long.valueOf(jwt.getSubject());

        OrderRequestResponseDTO order =
                orderRequestService.findById(requestId);

        // 판매자가 다른 구매자의 전체 매칭 상품을 열람하지 못하도록 제한한다.
        if (order.getBuyerId() == null
                || !order.getBuyerId().equals(currentUserId)) {

            throw new AccessDeniedException(
                    "본인이 등록한 주문의 매칭 결과만 조회할 수 있습니다."
            );
        }

        return ResponseEntity.ok(
                matchingService.findMatchesForOrder(
                        requestId
                )
        );
    }
}