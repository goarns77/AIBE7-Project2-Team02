package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.entity.AccountReportEntity;
import org.example.matcheat.domain.account.enums.AccountReportStatus;

import java.time.Instant;

public record AccountReportResponse(
        Long reportId,
        String title,
        String message,
        AccountReportStatus status,
        String adminResponse,
        Instant createdAt,
        Instant updatedAt,
        Instant reviewedAt) {
    public static AccountReportResponse from(AccountReportEntity report) {
        return new AccountReportResponse(
                report.getId(), report.getTitle(), report.getMessage(), report.getStatus(),
                report.getAdminResponse(), report.getCreatedAt(), report.getUpdatedAt(), report.getReviewedAt());
    }
}
