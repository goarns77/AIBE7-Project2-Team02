package org.example.matcheat.account.config;

import org.example.matcheat.account.adapter.security.NimbusAccessTokenIssuer;
import org.example.matcheat.account.domain.IssuedAccessToken;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.domain.UserRole;
import org.example.matcheat.account.domain.UserStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecuritySupportTest {
    @Test
    void issuesAndValidatesRequiredClaims() {
        AccountProperties properties = propertiesWithSecret("01234567890123456789012345678901");
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        SecretKey key = configuration.accountJwtSecretKey(properties);
        NimbusAccessTokenIssuer issuer = new NimbusAccessTokenIssuer(
                configuration.accountJwtEncoder(key), properties, Clock.systemUTC());
        JwtDecoder decoder = configuration.accountJwtDecoder(key, properties);

        IssuedAccessToken issued = issuer.issue(UserAccount.restore(
                42L, "user@example.com", "{bcrypt}hash", "홍길동", UserRole.USER, UserStatus.ACTIVE, 0, null));
        Jwt jwt = decoder.decode(issued.value());

        assertThat(issued.expiresInSeconds()).isEqualTo(3600);
        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getIssuer().toString()).isEqualTo("https://matcheat.local");
        assertThat(jwt.getAudience()).containsExactly("matcheat-api");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("USER");
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
        IssuedAccessToken issued = issuer.issue(UserAccount.restore(
                42L, "user@example.com", "{bcrypt}hash", "홍길동", UserRole.USER, UserStatus.ACTIVE, 0, null));

        AccountProperties wrongAudience = propertiesWithSecret("01234567890123456789012345678901");
        wrongAudience.getJwt().setAudience("other-api");
        JwtDecoder wrongAudienceDecoder = configuration.accountJwtDecoder(key, wrongAudience);

        assertThatThrownBy(() -> wrongAudienceDecoder.decode(issued.value())).isInstanceOf(JwtException.class);
        AccountProperties wrongIssuer = propertiesWithSecret("01234567890123456789012345678901");
        wrongIssuer.getJwt().setIssuer("https://other.local");
        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, wrongIssuer).decode(issued.value()))
                .isInstanceOf(JwtException.class);
        String tampered = issued.value().substring(0, issued.value().length() - 2) + "aa";
        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, issuerProperties).decode(tampered))
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
        IssuedAccessToken issued = issuer.issue(UserAccount.restore(
                42L, "user@example.com", "{bcrypt}hash", "홍길동", UserRole.USER, UserStatus.ACTIVE, 0, null));

        assertThatThrownBy(() -> configuration.accountJwtDecoder(key, properties).decode(issued.value()))
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
}
