package org.example.matcheat.domain.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 판매 조건 테스트 화면 이동을 담당하는 Controller
 */
@Controller
public class ProductPageController {

    @GetMapping("/product/list")
    public String listPage() {
        return "product/list";
    }

    @GetMapping("/product/new")
    public String createPage() {
        return "product/test";
    }

    @GetMapping("/product/update")
    public String updatePage() {
        return "product/update";
    }
}
