package org.example.matcheat.domain.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 판매 조건 테스트 화면 이동을 담당하는 Controller
 */
@Controller
public class ProductPageController {

    /**
     * 판매 조건 목록 테스트 페이지로 이동한다.
     */
    @GetMapping("/product")
    public String listPage() {
        return "product/list";
    }

    /**
     * 판매 조건 등록 테스트 페이지로 이동한다.
     */
    @GetMapping("/product/new")
    public String createPage() {
        return "product/create";
    }

    /**
     * 판매 조건 상세 테스트 페이지로 이동한다.
     */
    @GetMapping("/product/detail")
    public String detailPage() {
        return "product/detail";
    }

    /**
     * 판매 조건 수정 테스트 페이지로 이동한다.
     */
    @GetMapping("/product/update")
    public String updatePage() {
        return "product/update";
    }

    /**
     * 판매 추천 미리보기 페이지로 이동한다..
     */
    @GetMapping("/product/recommendations")
    public String recommendationPage() {
        return "product/recommendations";
    }
}
