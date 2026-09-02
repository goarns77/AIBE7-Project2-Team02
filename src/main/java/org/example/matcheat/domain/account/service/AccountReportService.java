package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.dto.AccountReportCreateRequest;
import org.example.matcheat.domain.account.dto.AccountReportResponse;
import org.example.matcheat.domain.account.dto.AdminAccountReportResponse;
import org.example.matcheat.domain.account.dto.AdminPageResponse;
import org.example.matcheat.domain.account.entity.AccountReportEntity;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.AccountReportStatus;
import org.example.matcheat.domain.account.repository.AccountReportRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AccountReportService {
    private static final int MAX_PAGE_SIZE = 100;

    private final AccountReportRepository reports;
    private final UserCredentialRepository users;
    private final Clock clock;

    public AccountReportService(AccountReportRepository reports, UserCredentialRepository users, Clock accountClock) {
        this.reports = reports;
        this.users = users;
        this.clock = accountClock;
    }

    @Transactional
    public AccountReportResponse create(long reporterId, AccountReportCreateRequest request) {
        requireUser(reporterId);
        AccountReportEntity report = AccountReportEntity.create(
                reporterId, request.title().trim(), request.message().trim(), clock.instant());
        return AccountReportResponse.from(reports.save(report));
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AccountReportResponse> mine(long reporterId, int page, int size) {
        Page<AccountReportEntity> result = reports.findByReporterId(reporterId, pageRequest(page, size));
        return pageResponse(result.map(AccountReportResponse::from));
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminAccountReportResponse> search(
            AccountReportStatus status, int page, int size) {
        Page<AccountReportEntity> result = reports.search(status, pageRequest(page, size));
        Page<AdminAccountReportResponse> responses = result.map(report ->
                AdminAccountReportResponse.from(report, requireUser(report.getReporterId())));
        return pageResponse(responses);
    }

    @Transactional
    public AdminAccountReportResponse review(
            long adminId, long reportId, AccountReportStatus status, String adminResponse) {
        if (status == null) {
            throw new AccountApplicationException(
                    AccountErrorCode.INVALID_REPORT_STATUS, "처리 상태를 선택해 주세요.");
        }
        AccountReportEntity report = reports.findById(reportId)
                .orElseThrow(() -> new AccountApplicationException(
                        AccountErrorCode.REPORT_NOT_FOUND, "신고를 찾을 수 없습니다."));
        String normalizedResponse = normalize(adminResponse);
        if ((status == AccountReportStatus.RESOLVED || status == AccountReportStatus.REJECTED)
                && normalizedResponse == null) {
            throw new AccountApplicationException(
                    AccountErrorCode.INVALID_REPORT_STATUS, "처리 완료 또는 반려 시 관리자 답변이 필요합니다.");
        }
        try {
            report.review(status, normalizedResponse, adminId, clock.instant());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new AccountApplicationException(AccountErrorCode.INVALID_REPORT_STATUS, exception.getMessage());
        }
        return AdminAccountReportResponse.from(report, requireUser(report.getReporterId()));
    }

    private UserAccount requireUser(long userId) {
        return users.findById(userId).orElseThrow(() -> new AccountApplicationException(
                AccountErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private static PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    private static <T> AdminPageResponse<T> pageResponse(Page<T> page) {
        return new AdminPageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
