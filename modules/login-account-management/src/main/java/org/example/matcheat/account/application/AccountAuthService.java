package org.example.matcheat.account.application;

import org.example.matcheat.account.domain.IssuedAccessToken;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserStatus;
import org.example.matcheat.account.port.in.CheckEmailAvailabilityUseCase;
import org.example.matcheat.account.port.in.LoginUseCase;
import org.example.matcheat.account.port.in.SignUpUseCase;
import org.example.matcheat.account.port.out.AccessTokenIssuer;
import org.example.matcheat.account.port.out.DuplicateUserEmailException;
import org.example.matcheat.account.port.out.PasswordHasher;
import org.example.matcheat.account.port.out.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountAuthService implements SignUpUseCase, LoginUseCase, CheckEmailAvailabilityUseCase {
    private final UserCredentialRepository repository;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer tokenIssuer;

    public AccountAuthService(
            UserCredentialRepository repository,
            PasswordHasher passwordHasher,
            AccessTokenIssuer tokenIssuer) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        String email = EmailNormalizer.normalize(command.email());
        String name = normalizeName(command.name());
        PasswordPolicy.validate(command.password());
        if (!command.password().equals(command.passwordConfirm())) {
            throw new AccountApplicationException(
                    AccountErrorCode.PASSWORD_CONFIRM_MISMATCH,
                    "비밀번호 확인이 일치하지 않습니다.");
        }
        if (repository.existsByEmail(email)) {
            throw emailAlreadyExists();
        }

        UserAccount account = UserAccount.registerUser(email, passwordHasher.hash(command.password()), name);
        try {
            UserAccount saved = repository.save(account);
            return new SignUpResult(saved.id(), saved.email(), saved.name(), saved.role(), saved.status());
        } catch (DuplicateUserEmailException exception) {
            throw emailAlreadyExists();
        }
    }

    @Override
    public LoginResult login(LoginCommand command) {
        String email = EmailNormalizer.normalize(command.email());
        UserAccount account = repository.findByEmail(email)
                .orElseThrow(AccountAuthService::invalidCredentials);

        if (account.passwordHash() == null || !passwordHasher.matches(command.password(), account.passwordHash())) {
            throw invalidCredentials();
        }
        ensureActive(account.status());

        IssuedAccessToken token = tokenIssuer.issue(account);
        return new LoginResult(
                token.value(),
                "Bearer",
                token.expiresInSeconds(),
                new UserSummary(account.id(), account.email(), account.name(), account.role()));
    }

    @Override
    public EmailAvailability check(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        return new EmailAvailability(email, !repository.existsByEmail(email));
    }

    private static String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty() || normalized.length() > 50) {
            throw new AccountApplicationException(AccountErrorCode.VALIDATION_FAILED, "이름을 확인해 주세요.");
        }
        return normalized;
    }

    private static void ensureActive(UserStatus status) {
        if (status == UserStatus.SUSPENDED) {
            throw new AccountApplicationException(AccountErrorCode.ACCOUNT_SUSPENDED, "정지된 계정입니다.");
        }
        if (status == UserStatus.WITHDRAWN) {
            throw new AccountApplicationException(AccountErrorCode.ACCOUNT_WITHDRAWN, "탈퇴한 계정입니다.");
        }
    }

    private static AccountApplicationException invalidCredentials() {
        return new AccountApplicationException(
                AccountErrorCode.INVALID_CREDENTIALS,
                "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    private static AccountApplicationException emailAlreadyExists() {
        return new AccountApplicationException(
                AccountErrorCode.EMAIL_ALREADY_EXISTS,
                "이미 사용 중인 이메일입니다.");
    }
}
