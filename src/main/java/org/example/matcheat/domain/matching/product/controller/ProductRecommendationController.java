package org.example.matcheat.domain.matching.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.matching.product.dto.MatchedOrderResponseDTO;
import org.example.matcheat.domain.matching.product.service.ProductRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
/**
 * 판매 조건 1건에 대한 주문 요청 추천 API를 제공하는 컨트롤러이다.
 */
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;

    /**
     * 특정 판매 조건에 대한 추천 주문 요청 목록을 조회한다. 요청자가 그 판매 조건의 소유자인 경우에만 허용한다.
     */
    @GetMapping("/{productId}/order-requests/recommendations")
    public ResponseEntity<List<MatchedOrderResponseDTO>> recommend(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ResponseEntity.ok(productRecommendationService.recommend(productId, userId));
    }
}
