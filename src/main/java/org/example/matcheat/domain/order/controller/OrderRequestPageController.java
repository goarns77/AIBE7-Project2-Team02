package org.example.matcheat.domain.order.controller;

import org.example.matcheat.domain.order.dto.OrderRequestCreateDTO;
import org.example.matcheat.domain.order.dto.OrderRequestUpdateDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 주문 요청 관련 Thymeleaf 화면 이동을 담당하는 Controller
 */
@Controller
public class OrderRequestPageController {
    private final OrderRequestService orderRequestService;

    public OrderRequestPageController(OrderRequestService orderRequestService) {
        this.orderRequestService = orderRequestService;
    }

    /**
     * 전체 주문 목록을 조회하고, 검색어가 있으면 제목 또는 음식 카테고리로 검색
     */
    @GetMapping("/requests")
    public String listPage(
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        if (keyword == null || keyword.isBlank()) {
            model.addAttribute(
                    "orderRequests",
                    orderRequestService.findAll()
            );
        } else {
            model.addAttribute(
                    "orderRequests",
                    orderRequestService.searchByKeyword(keyword)
            );
        }

        model.addAttribute("keyword", keyword);

        return "orderrequest/list";
    }

    /**
     * 주문 요청 상세 페이지를 반환
     */
    @GetMapping("/requests/{id}")
    public String detailPage(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("requestId", id);

        return "orderrequest/detail";
    }

    /**
     * 주문 요청 등록 페이지를 반환
     */
    @GetMapping("/requests/new")
    public String createPage(Model model) {
        model.addAttribute("orderRequest", new OrderRequestCreateDTO());

        return "orderrequest/create";
    }

    /**
     * 주문 요청 수정 페이지를 반환
     */
    @GetMapping("/requests/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("orderRequest", new OrderRequestUpdateDTO());
        model.addAttribute("requestId", id);

        return "orderrequest/edit";
    }
}
