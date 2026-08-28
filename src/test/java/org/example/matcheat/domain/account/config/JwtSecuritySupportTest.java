package org.example.matcheat.domain.account.config;

import org.example.matcheat.domain.account.security.NimbusAccessTokenIssuer;
import org.example.matcheat.domain.account.security.AccountJwtAccountValidator;
import org.example.matcheat.domain.account.entity.IssuedAccessToken;
import org.example.matcheat.domain.account.entity.UserAccount;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.repository.UserCredentialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecuritySupportTest {
    @Test
    void issuesAndValidatesRequiredClaims() {
        AccountProperties properties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(properties);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), properties, Clock.systemUTC());
        UserAccount account = account(UserRole.USER, UserStatus.ACTIVE, 7);
        JwtDecoder decoder = configuration.accountJwtDecoder(key, properties, validatorFor(account));

        IssuedAccessToken issued = issuer.issue(account);
        Jwt jwt = decoder.decode(issued.value());

        assertThat(issued.expiresInSeconds()).isEqualTo(3600);
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://matcheat.local");
        assertThat(jwt.getAudience()).containsExactly("matcheat-api");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("USER");
        assertThat(((Number) jwt.getClaim("ver")).intValue()).isEqualTo(7);
        assertThat(jwt.getId()).isNotBlank();
        assertThat(configuration.accountJwtAuthenticationConverter().convert(jwt).getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void rejectsWrongAudienceAndTamperedToken() {
        AccountProperties issuerProperties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(issuerProperties);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), issuerProperties, Clock.systemUTC());
        UserAccount account = account(UserRole.USER, UserStatus.ACTIVE, 0);
        AccountJwtAccountValidator validator = validatorFor(account);
        IssuedAccessToken issued = issuer.issue(account);

        AccountProperties wrongAudience = propertiesWithSecret("01234567890123456789012345678901");
        wrongAudience.getJwt().setAudience("other-api");
        JwtDecoder wrongAudienceDecoder = configuration.accountJwtDecoder(key, wrongAudience, validator);

        assertThatThrownBy(() -> wrongAudienceDecoder.decode(issued.value())).isInstanceOf(JwtException.class);
        AccountProperties wrongIssuer = propertiesWithSecret("01234567890123456789012345678901");
        wrongIssuer.getJwt().setIssuer("https://other.local");
        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, wrongIssuer, validator).decode(issued.value()))
                .isInstanceOf(JwtException.class);
        String tampered = issued.value().substring(0, issued.value().length() - 2) + "aa";
        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, issuerProperties, validator).decode(tampered))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        AccountProperties properties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(properties);
        Clock expiredClock = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), properties, expiredClock);
        UserAccount account = account(UserRole.USER, UserStatus.ACTIVE, 0);
        IssuedAccessToken issued = issuer.issue(account);

        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, properties, validatorFor(account))
                        .decode(issued.value()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsStaleVersionInactiveAccountAndChangedRole() {
        AccountProperties properties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(properties);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), properties, Clock.systemUTC());
        IssuedAccessToken issued = issuer.issue(account(UserRole.USER, UserStatus.ACTIVE, 0));

        assertRejected(configuration, key, properties, issued, account(UserRole.USER, UserStatus.ACTIVE, 1));
        assertRejected(configuration, key, properties, issued, account(UserRole.USER, UserStatus.SUSPENDED, 0));
        assertRejected(configuration, key, properties, issued, account(UserRole.ADMIN, UserStatus.ACTIVE, 0));
    }

    @Test
    void rejectsTokenForMissingAccount() {
        AccountProperties properties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(properties);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), properties, Clock.systemUTC());
        IssuedAccessToken issued = issuer.issue(account(UserRole.USER, UserStatus.ACTIVE, 0));
        UserCredentialRepository repository = mock(UserCredentialRepository.class);

        assertThatThrownBy(() -> configuration.accountJwtDecoder(
                        key, properties, new AccountJwtAccountValidator(repository)).decode(issued.value()))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingInvalidAndShortSecrets() {
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        AccountProperties missing = new AccountProperties();
        assertThatThrownBy(() -> configuration.accountJwtSecretKey(missing))
                .isInstanceOf(IllegalStateException.class);

        AccountProperties invalid = new AccountProperties();
        invalid.getJwt().setSecret("not-base64!");
        assertThatThrownBy(() -> configuration.accountJwtSecretKey(invalid))
                .isInstanceOf(IllegalStateException.class);

        AccountProperties shortKey = propertiesWithSecret("too-short");
        assertThatThrownBy(() -> configuration.accountJwtSecretKey(shortKey))
                .isInstanceOf(IllegalStateException.class);
    }

    private static AccountProperties propertiesWithSecret(String secret) {
        AccountProperties properties = new AccountProperties();
        properties.getJwt().setSecret(Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8)));
        properties.getJwt().setAccessTokenTtl(Duration.ofHours(1));
        return properties;
    }

    private static UserAccount account(UserRole role, UserStatus status, int tokenVersion) {
        return UserAccount.restore(
                42L, "user@example.com", "{bcrypt}hash", "홍길동", role, status, tokenVersion, null);
    }

    private static AccountJwtAccountValidator validatorFor(UserAccount account) {
        UserCredentialRepository repository = mock(UserCredentialRepository.class);
        when(repository.findById(account.id())).thenReturn(Optional.of(account));
        return new AccountJwtAccountValidator(repository);
    }

    private static void assertRejected(
            AccountSecuritySupportConfiguration configuration,
            SecretKey key,
            AccountProperties properties,
            IssuedAccessToken issued,
            UserAccount currentAccount) {
        assertThatThrownBy(() -> configuration.accountJwtDecoder(
                        key, properties, validatorFor(currentAccount)).decode(issued.value()))
                .isInstanceOf(JwtException.class);
    }
}
