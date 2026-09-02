package org.example.matcheat.domain.account.controller;

import jakarta.validation.Valid;
import org.example.matcheat.domain.account.dto.AccountReportReviewRequest;
import org.example.matcheat.domain.account.dto.AdminAccountReportResponse;
import org.example.matcheat.domain.account.dto.AdminPageResponse;
import org.example.matcheat.domain.account.enums.AccountReportStatus;
import org.example.matcheat.domain.account.service.AccountReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminAccountReportRestController {
    private final AccountReportService service;

    public AdminAccountReportRestController(AccountReportService service) {
        this.service = service;
    }

    @GetMapping
    public AdminPageResponse<AdminAccountReportResponse> reports(
            @RequestParam(required = false) AccountReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(status, page, size);
    }

    @PatchMapping("/{reportId}")
    public AdminAccountReportResponse review(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable long reportId,
            @Valid @RequestBody AccountReportReviewRequest request) {
        return service.review(
                Long.parseLong(jwt.getSubject()), reportId, request.status(), request.adminResponse());
    }
}
