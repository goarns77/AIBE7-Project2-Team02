package org.example.matcheat.domain.account.security;

import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountJwtAccountValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error(
            "invalid_token",
            "유효하지 않은 인증 토큰입니다.",
            null);

    private final UserCredentialRepository repository;

    public AccountJwtAccountValidator(UserCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Optional<Long> userId = parseUserId(jwt.getSubject());
        Optional<Integer> tokenVersion = parseTokenVersion(jwt.getClaim("ver"));
        String role = jwt.getClaimAsString("role");
        if (userId.isEmpty() || tokenVersion.isEmpty() || role == null) {
            return failure();
        }

        return repository.findById(userId.get())
                .filter(account -> isCurrentAccount(account, tokenVersion.get(), role))
                .map(account -> OAuth2TokenValidatorResult.success())
                .orElseGet(AccountJwtAccountValidator::failure);
    }

    private static boolean isCurrentAccount(UserAccount account, int tokenVersion, String role) {
        return account.status() == UserStatus.ACTIVE
                && account.tokenVersion() == tokenVersion
                && account.role().name().equals(role);
    }

    private static Optional<Long> parseUserId(String subject) {
        try {
            long userId = Long.parseLong(subject);
            return userId > 0 ? Optional.of(userId) : Optional.empty();
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseTokenVersion(Object claim) {
        if (!(claim instanceof Number number)) {
            return Optional.empty();
        }
        long value = number.longValue();
        return value >= 0 && value <= Integer.MAX_VALUE
                ? Optional.of((int) value)
                : Optional.empty();
    }

    private static OAuth2TokenValidatorResult failure() {
        return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
    }
}
