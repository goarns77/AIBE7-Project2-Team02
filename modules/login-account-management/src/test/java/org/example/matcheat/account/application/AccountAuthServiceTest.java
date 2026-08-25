package org.example.matcheat.account.application;

import org.example.matcheat.account.domain.IssuedAccessToken;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserStatus;
import org.example.matcheat.account.port.in.LoginUseCase;
import org.example.matcheat.account.port.in.SignUpUseCase;
import org.example.matcheat.account.port.out.AccessTokenIssuer;
import org.example.matcheat.account.port.out.DuplicateUserEmailException;
import org.example.matcheat.account.port.out.PasswordHasher;
import org.example.matcheat.account.port.out.UserCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAuthServiceTest {
    @Mock
    private UserCredentialRepository repository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private AccessTokenIssuer tokenIssuer;

    private AccountAuthService service;

    @BeforeEach
    void setUp() {
        service = new AccountAuthService(repository, passwordHasher, tokenIssuer);
    }

    @Test
    void signsUpWithNormalizedEmailAndHashedPassword() {
        when(passwordHasher.hash("password1234")).thenReturn("{bcrypt}hash");
        when(repository.save(any())).thenAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            return UserAccount.restore(
                    7L,
                    account.email(),
                    account.passwordHash(),
                    account.name(),
                    account.role(),
                    account.status(),
                    account.tokenVersion(),
                    account.withdrawnAt());
        });

        SignUpUseCase.SignUpResult result = service.signUp(new SignUpUseCase.SignUpCommand(
                "  USER@Example.COM ", "password1234", "password1234", " 홍길동 "));

        assertThat(result.userId()).isEqualTo(7L);
        assertThat(result.email()).isEqualTo("user@example.com");
        assertThat(result.name()).isEqualTo("홍길동");

        ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().passwordHash()).isEqualTo("{bcrypt}hash");
    }

    @Test
    void rejectsDuplicateEmailBeforeSave() {
        when(repository.existsByEmail("user@example.com")).thenReturn(true);

        assertAccountError(
                () -> service.signUp(new SignUpUseCase.SignUpCommand(
                        "user@example.com", "password1234", "password1234", "홍길동")),
                AccountErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void translatesConcurrentUniqueConstraintFailure() {
        when(passwordHasher.hash("password1234")).thenReturn("{bcrypt}hash");
        when(repository.save(any())).thenThrow(new DuplicateUserEmailException(new RuntimeException()));

        assertAccountError(
                () -> service.signUp(new SignUpUseCase.SignUpCommand(
                        "user@example.com", "password1234", "password1234", "홍길동")),
                AccountErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void rejectsPasswordMismatchAndWeakPassword() {
        assertAccountError(
                () -> service.signUp(new SignUpUseCase.SignUpCommand(
                        "user@example.com", "password1234", "different123", "홍길동")),
                AccountErrorCode.PASSWORD_CONFIRM_MISMATCH);
        assertAccountError(
                () -> service.signUp(new SignUpUseCase.SignUpCommand(
                        "user@example.com", "onlyletters", "onlyletters", "홍길동")),
                AccountErrorCode.VALIDATION_FAILED);
    }

    @Test
    void logsInAndReturnsIssuedToken() {
        UserAccount account = activeAccount();
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("password1234", "{bcrypt}hash")).thenReturn(true);
        when(tokenIssuer.issue(account)).thenReturn(new IssuedAccessToken("token", 3600));

        LoginUseCase.LoginResult result = service.login(
                new LoginUseCase.LoginCommand("USER@example.com", "password1234"));

        assertThat(result.accessToken()).isEqualTo("token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(3600);
        assertThat(result.user().userId()).isEqualTo(7L);
    }

    @Test
    void missingUserAndWrongPasswordUseSameErrorCode() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertAccountError(
                () -> service.login(new LoginUseCase.LoginCommand("missing@example.com", "password1234")),
                AccountErrorCode.INVALID_CREDENTIALS);

        UserAccount account = activeAccount();
        when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(account));
        when(passwordHasher.matches("wrong1234", "{bcrypt}hash")).thenReturn(false);
        assertAccountError(
                () -> service.login(new LoginUseCase.LoginCommand("user@example.com", "wrong1234")),
                AccountErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void rejectsSuspendedAndWithdrawnAccountsAfterPasswordCheck() {
        when(passwordHasher.matches("password1234", "{bcrypt}hash")).thenReturn(true);
        when(repository.findByEmail("suspended@example.com")).thenReturn(Optional.of(UserAccount.restore(
                8L, "suspended@example.com", "{bcrypt}hash", "정지", accountRole(), UserStatus.SUSPENDED, 0, null)));
        assertAccountError(
                () -> service.login(new LoginUseCase.LoginCommand("suspended@example.com", "password1234")),
                AccountErrorCode.ACCOUNT_SUSPENDED);

        when(repository.findByEmail("withdrawn@example.com")).thenReturn(Optional.of(UserAccount.restore(
                9L, "withdrawn@example.com", "{bcrypt}hash", "탈퇴", accountRole(), UserStatus.WITHDRAWN, 1, Instant.now())));
        assertAccountError(
                () -> service.login(new LoginUseCase.LoginCommand("withdrawn@example.com", "password1234")),
                AccountErrorCode.ACCOUNT_WITHDRAWN);
    }

    @Test
    void validatesEmailAvailabilityInput() {
        assertAccountError(() -> service.check("not-an-email"), AccountErrorCode.INVALID_EMAIL);
    }

    private static UserAccount activeAccount() {
        return UserAccount.restore(
                7L, "user@example.com", "{bcrypt}hash", "홍길동", accountRole(), UserStatus.ACTIVE, 0, null);
    }

    private static org.example.matcheat.account.domain.UserRole accountRole() {
        return org.example.matcheat.account.domain.UserRole.USER;
    }

    private static void assertAccountError(Runnable runnable, AccountErrorCode code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(AccountApplicationException.class)
                .extracting(exception -> ((AccountApplicationException) exception).code())
                .isEqualTo(code);
    }
}
