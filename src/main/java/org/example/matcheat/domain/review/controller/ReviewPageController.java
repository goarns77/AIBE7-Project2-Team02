package org.example.matcheat.domain.review.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
/**
 * 리뷰 화면(Thymeleaf 페이지) 라우팅만 담당하는 컨트롤러이다. 실제 데이터는
 * 각 템플릿의 JS가 /api/v1/reviews 계열 API를 직접 호출해 채운다.
 */
public class ReviewPageController {

    /**
     * 리뷰 작성 페이지로 이동한다. paymentId는 페이지 내 JS가 쿼리스트링에서 직접 읽어 사용한다.
     */
    @GetMapping("/reviews/new")
    public String createPage() {
        return "review/create";
    }

    /**
     * 특정 판매자가 받은 리뷰 목록 페이지로 이동한다. sellerId는 페이지 내 JS가
     * 쿼리스트링에서 직접 읽어 사용한다.
     */
    @GetMapping("/reviews")
    public String listPage() {
        return "review/list";
    }
}
