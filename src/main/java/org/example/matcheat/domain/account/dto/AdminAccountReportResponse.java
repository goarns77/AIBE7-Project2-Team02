package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.entity.AccountReportEntity;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.AccountReportStatus;

import java.time.Instant;

public record AdminAccountReportResponse(
        Long reportId,
        Long reporterId,
        String reporterName,
        String reporterEmail,
        String title,
        String message,
        AccountReportStatus status,
        String adminResponse,
        Long reviewedBy,
        Instant createdAt,
        Instant updatedAt,
        Instant reviewedAt) {
    public static AdminAccountReportResponse from(AccountReportEntity report, UserAccount reporter) {
        return new AdminAccountReportResponse(
                report.getId(), report.getReporterId(), reporter.name(), reporter.email(),
                report.getTitle(), report.getMessage(), report.getStatus(), report.getAdminResponse(),
                report.getReviewedBy(), report.getCreatedAt(), report.getUpdatedAt(), report.getReviewedAt());
    }
}
