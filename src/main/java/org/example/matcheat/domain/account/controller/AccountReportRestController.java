package org.example.matcheat.domain.account.controller;

import jakarta.validation.Valid;
import org.example.matcheat.domain.account.dto.AccountReportCreateRequest;
import org.example.matcheat.domain.account.dto.AccountReportResponse;
import org.example.matcheat.domain.account.dto.AdminPageResponse;
import org.example.matcheat.domain.account.service.AccountReportService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class AccountReportRestController {
    private final AccountReportService service;

    public AccountReportRestController(AccountReportService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountReportResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountReportCreateRequest request) {
        return service.create(userId(jwt), request);
    }

    @GetMapping("/mine")
    public AdminPageResponse<AccountReportResponse> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.mine(userId(jwt), page, size);
    }

    private static long userId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
