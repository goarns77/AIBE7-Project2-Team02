package org.example.matcheat.domain.order.controller;

import jakarta.validation.Valid;
import org.example.matcheat.domain.order.dto.OrderRequestCreateDTO;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.dto.OrderRequestUpdateDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
     * 전체 주문 요청 목록 페이지를 반환
     */
    @GetMapping("/requests")
    public String listPage(Model model) {
        model.addAttribute("orderRequests", orderRequestService.findAll());

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
        model.addAttribute("orderRequest", orderRequestService.findById(id));

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
     * 주문 요청 등록 폼을 처리
     */
    @PostMapping("/requests")
    public String create(
            @Valid @ModelAttribute("orderRequest") OrderRequestCreateDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "orderrequest/create";
        }

        OrderRequestResponseDTO createdOrderRequest = orderRequestService.create(dto);

        return "redirect:/requests/" + createdOrderRequest.getId();
    }

    /**
     * 주문 요청 수정 페이지를 반환
     */
    @GetMapping("/requests/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            Model model
    ) {
        OrderRequestResponseDTO orderRequest = orderRequestService.findById(id);

        model.addAttribute(
                "orderRequest",
                OrderRequestUpdateDTO.from(orderRequest)
        );
        model.addAttribute("requestId", id);

        return "orderrequest/edit";
    }

    /**
     * 주문 요청 수정 폼을 처리
     */
    @PostMapping("/requests/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("orderRequest") OrderRequestUpdateDTO dto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestId", id);
            return "orderrequest/edit";
        }

        orderRequestService.update(id, dto);

        return "redirect:/requests/" + id;
    }

    /**
     * 주문 요청 취소 요청을 처리
     */
    @PostMapping("/requests/{id}/cancel")
    public String cancel(
            @PathVariable Long id
    ) {
        orderRequestService.cancel(id);

        return "redirect:/requests/" + id;
    }
}
