package org.example.matcheat.domain.order.controller;

import org.example.matcheat.domain.order.dto.OrderRequestCreateDTO;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.dto.OrderRequestUpdateDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private static final int ORDER_PAGE_SIZE = 5;
    private final OrderRequestService orderRequestService;

    public OrderRequestPageController(OrderRequestService orderRequestService) {
        this.orderRequestService = orderRequestService;
    }

    /**
     * 전체 주문 목록을 페이지 단위로 조회하고,
     * 검색어가 있으면 제목 또는 음식 카테고리로 검색한다.
     */
    @GetMapping("/requests")
    public String listPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int safePage = Math.max(page, 0);

        PageRequest pageable = PageRequest.of(
                safePage,
                ORDER_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<OrderRequestResponseDTO> orderPage;

        if (keyword == null || keyword.isBlank()) {
            orderPage = orderRequestService.findAll(pageable);
        } else {
            orderPage = orderRequestService.searchByKeyword(
                    keyword.trim(),
                    pageable
            );
        }

        int totalPages = orderPage.getTotalPages();
        int currentPage = orderPage.getNumber();

        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, startPage + 4);

        if (endPage - startPage < 4) {
            startPage = Math.max(0, endPage - 4);
        }

        model.addAttribute("orderRequests", orderPage.getContent());
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
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
