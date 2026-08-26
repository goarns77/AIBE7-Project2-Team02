package org.example.matcheat.orderrequest.controller;

import jakarta.validation.Valid;
import org.example.matcheat.orderrequest.dto.OrderRequestCreateDTO;
import org.example.matcheat.orderrequest.service.OrderRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

        orderRequestService.create(dto);

        return "redirect:/requests/new?success";
    }
}
