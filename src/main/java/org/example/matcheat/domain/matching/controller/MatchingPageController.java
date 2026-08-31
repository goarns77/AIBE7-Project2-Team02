package org.example.matcheat.domain.matching.controller;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 주문 기준 상품 매칭 결과 화면으로 이동하는 요청을 처리한다.
 */
@Controller
@RequiredArgsConstructor
public class MatchingPageController {

    private final OrderRequestService orderRequestService;

    /**
     * 특정 주문의 맞춤 상품 매칭 결과 화면을 반환한다.
     */
    @GetMapping("/requests/{requestId}/matches")
    public String matchesPage(
            @PathVariable Long requestId,
            Model model
    ) {
        model.addAttribute(
                "orderRequest",
                orderRequestService.findById(requestId)
        );

        model.addAttribute(
                "requestId",
                requestId
        );

        return "matching/result";
    }
}