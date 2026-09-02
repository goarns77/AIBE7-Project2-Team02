package org.example.matcheat.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.order.dto.TradeActivityResponse;
import org.example.matcheat.domain.order.service.TradeActivityQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class TradeActivityController {
    private final TradeActivityQueryService tradeActivities;

    @GetMapping("/purchases")
    public ResponseEntity<List<TradeActivityResponse>> findPurchases(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(tradeActivities.findPurchases(currentAccountId(jwt)));
    }

    @GetMapping("/sales")
    public ResponseEntity<List<TradeActivityResponse>> findSales(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(tradeActivities.findSales(currentAccountId(jwt)));
    }

    private static long currentAccountId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
