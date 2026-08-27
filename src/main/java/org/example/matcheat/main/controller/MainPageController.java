package org.example.matcheat.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MatchEAT 메인 Thymeleaf 화면 이동을 담당하는 Controller
 */
@Controller
public class MainPageController {
    /**
     * 메인 페이지를 반환
     */
    @GetMapping("/")
    public String mainPage() {
        return "main/index";
    }
}
