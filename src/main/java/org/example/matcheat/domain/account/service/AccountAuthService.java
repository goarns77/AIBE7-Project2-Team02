package org.example.matcheat.domain.account.service;

import org.example.matcheat.domain.account.entity.IssuedAccessToken;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.security.AccessTokenIssuer;
import org.example.matcheat.domain.account.repository.DuplicateUserEmailException;
import org.example.matcheat.domain.account.security.PasswordHasher;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountAuthService {
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

    @Transactional
    public SignUpResult signUp(String rawEmail, String password, String passwordConfirm, String rawName) {
        String email = EmailNormalizer.normalize(rawEmail);
        String name = NameNormalizer.normalize(rawName);
        PasswordPolicy.validate(password);
        if (!password.equals(passwordConfirm)) {
            throw new AccountApplicationException(
                    AccountErrorCode.PASSWORD_CONFIRM_MISMATCH,
                    "비밀번호 확인이 일치하지 않습니다.");
        }
        if (repository.existsByEmail(email)) {
            throw emailAlreadyExists();
        }

        UserAccount account = UserAccount.registerUser(email, passwordHasher.hash(password), name);
        try {
            UserAccount saved = repository.save(account);
            return new SignUpResult(saved.id(), saved.email(), saved.name(), saved.role(), saved.status());
        } catch (DuplicateUserEmailException exception) {
            throw emailAlreadyExists();
        }
    }

    public LoginResult login(String rawEmail, String password) {
        String email = EmailNormalizer.normalize(rawEmail);
        UserAccount account = repository.findByEmail(email)
                .orElseThrow(AccountAuthService::invalidCredentials);

        if (account.passwordHash() == null || !passwordHasher.matches(password, account.passwordHash())) {
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

    public EmailAvailability checkEmailAvailability(String rawEmail) {
        String email = EmailNormalizer.normalize(rawEmail);
        return new EmailAvailability(email, !repository.existsByEmail(email));
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

    public record SignUpResult(Long userId, String email, String name, UserRole role, UserStatus status) {
    }

    public record LoginResult(
            String accessToken,
            String tokenType,
            long expiresIn,
            UserSummary user) {
    }

    public record UserSummary(Long userId, String email, String name, UserRole role) {
    }

    public record EmailAvailability(String email, boolean available) {
    }
}
