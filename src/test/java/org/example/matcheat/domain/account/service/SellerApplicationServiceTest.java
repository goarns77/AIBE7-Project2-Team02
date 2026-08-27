package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.SellerApplicationRepository;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-27T00:00:00Z");

    @Mock
    private UserCredentialRepository users;
    @Mock
    private SellerApplicationRepository sellerApplications;

    private SellerApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SellerApplicationService(
                users,
                sellerApplications,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsPendingApplicationWithNormalizedBusinessData() {
        when(users.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(sellerApplications.save(
                7L,
                "매치잇 상회",
                "1234567890",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                new BigDecimal("10.00"),
                NOW)).thenReturn(new SellerApplicationRepository.SellerApplication(
                3L, SellerVerificationStatus.PENDING));

        SellerApplicationService.ApplicationResult result = service.apply(
                7L,
                "  매치잇 상회  ",
                "123-45-67890",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                new BigDecimal("10.00"));

        assertThat(result.sellerId()).isEqualTo(3L);
        assertThat(result.status()).isEqualTo(SellerVerificationStatus.PENDING);
        verify(sellerApplications).save(
                7L,
                "매치잇 상회",
                "1234567890",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                new BigDecimal("10.00"),
                NOW);
    }

    @Test
    void rejectsExistingApplication() {
        when(users.findById(7L)).thenReturn(Optional.of(activeUser()));
        when(sellerApplications.existsByUserId(7L)).thenReturn(true);

        assertAccountError(() -> service.apply(
                7L, "매치잇", "1234567890", null, null, null),
                AccountErrorCode.SELLER_APPLICATION_ALREADY_EXISTS);
    }

    @Test
    void rejectsIncompleteCoordinatesAndInvalidBusinessNumber() {
        when(users.findById(7L)).thenReturn(Optional.of(activeUser()));

        assertAccountError(() -> service.apply(
                7L, "매치잇", "invalid", null, null, null),
                AccountErrorCode.VALIDATION_FAILED);
        assertAccountError(() -> service.apply(
                7L, "매치잇", "1234567890", new BigDecimal("37.5"), null, null),
                AccountErrorCode.VALIDATION_FAILED);
    }

    private static UserAccount activeUser() {
        return UserAccount.restore(
                7L,
                "user@example.com",
                "{bcrypt}hash",
                "홍길동",
                UserRole.USER,
                UserStatus.ACTIVE,
                0,
                null);
    }

    private static void assertAccountError(Runnable runnable, AccountErrorCode code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(AccountApplicationException.class)
                .extracting(exception -> ((AccountApplicationException) exception).code())
                .isEqualTo(code);
    }
}
