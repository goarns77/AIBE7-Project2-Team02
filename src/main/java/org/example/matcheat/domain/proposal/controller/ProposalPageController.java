package org.example.matcheat.domain.proposal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 수주 제안 관련 Thymeleaf 화면 이동을 담당한다.
 */
@Controller
@RequiredArgsConstructor
public class ProposalPageController {

    /**
     * 특정 주문에 새로운 수주 제안을 작성하는 화면을 보여준다.
     */
    @GetMapping("/requests/{requestId}/proposals/new")
    public String createPage(
            @PathVariable Long requestId,
            Model model
    ) {
        model.addAttribute("requestId", requestId);

        return "proposal/create";
    }

    /**
     * 현재 사용자의 받은 제안과 보낸 제안을 관리하는 화면을 보여준다.
     */
    @GetMapping("/proposals")
    public String proposalListPage() {
        return "proposal/list";
    }
}
