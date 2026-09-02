package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.dto.AccountReportCreateRequest;
import org.example.matcheat.domain.account.entity.AccountReportEntity;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.AccountReportStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.AccountReportRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountReportServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-02T03:00:00Z");

    private AccountReportRepository reports;
    private UserCredentialRepository users;
    private AccountReportService service;

    @BeforeEach
    void setUp() {
        reports = mock(AccountReportRepository.class);
        users = mock(UserCredentialRepository.class);
        service = new AccountReportService(reports, users, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsReportForAuthenticatedUserAndNormalizesMessage() {
        when(users.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(reports.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(7L, new AccountReportCreateRequest("  거래 문의  ", "  확인해 주세요.  "));

        ArgumentCaptor<AccountReportEntity> captor = ArgumentCaptor.forClass(AccountReportEntity.class);
        verify(reports).save(captor.capture());
        assertThat(captor.getValue().getReporterId()).isEqualTo(7L);
        assertThat(response.title()).isEqualTo("거래 문의");
        assertThat(response.message()).isEqualTo("확인해 주세요.");
        assertThat(response.status()).isEqualTo(AccountReportStatus.PENDING);
    }

    @Test
    void requiresAdminResponseWhenClosingReport() {
        AccountReportEntity report = AccountReportEntity.create(7L, "신고", "내용", NOW);
        when(reports.findById(3L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.review(1L, 3L, AccountReportStatus.RESOLVED, " "))
                .isInstanceOfSatisfying(AccountApplicationException.class,
                        exception -> assertThat(exception.code()).isEqualTo(AccountErrorCode.INVALID_REPORT_STATUS));
    }

    @Test
    void preventsChangingTerminalReport() {
        AccountReportEntity report = AccountReportEntity.create(7L, "신고", "내용", NOW);
        report.review(AccountReportStatus.RESOLVED, "처리했습니다.", 1L, NOW);
        when(reports.findById(3L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.review(1L, 3L, AccountReportStatus.IN_REVIEW, null))
                .isInstanceOfSatisfying(AccountApplicationException.class,
                        exception -> assertThat(exception.code()).isEqualTo(AccountErrorCode.INVALID_REPORT_STATUS));
    }

    @Test
    void returnsNotFoundForMissingReport() {
        when(reports.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.review(1L, 99L, AccountReportStatus.IN_REVIEW, null))
                .isInstanceOfSatisfying(AccountApplicationException.class,
                        exception -> assertThat(exception.code()).isEqualTo(AccountErrorCode.REPORT_NOT_FOUND));
    }

    private static UserAccount user(long id) {
        return UserAccount.restore(
                id, "user@example.com", "hash", "사용자", UserRole.USER, UserStatus.ACTIVE, 0, null);
    }
}
