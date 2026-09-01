package org.example.matcheat.domain.account.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.example.matcheat.domain.account.security.AccountJwtAccountValidator;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AccountProperties.class)
public class AccountSecuritySupportConfiguration {
    @Bean("accountPasswordEncoder")
    PasswordEncoder accountPasswordEncoder(AccountProperties properties) {
        if (!"bcrypt".equalsIgnoreCase(properties.getPasswordEncoder())) {
            throw new IllegalStateException("현재 지원하는 비밀번호 인코더는 bcrypt입니다.");
        }
        return new DelegatingPasswordEncoder(
                "bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder(10)));
    }

    @Bean("accountJwtSecretKey")
    SecretKey accountJwtSecretKey(AccountProperties properties) {
        String encodedSecret = properties.getJwt().getSecret();
        if (encodedSecret == null || encodedSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET 환경변수가 필요합니다.");
        }

        byte[] secret;
        try {
            secret = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET은 Base64 형식이어야 합니다.", exception);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET은 디코딩 후 최소 256비트여야 합니다.");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean("accountJwtEncoder")
    JwtEncoder accountJwtEncoder(@Qualifier("accountJwtSecretKey") SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
    }

    @Bean("accountJwtDecoder")
    JwtDecoder accountJwtDecoder(
            @Qualifier("accountJwtSecretKey") SecretKey secretKey,
            AccountProperties properties,
            AccountJwtAccountValidator accountValidator) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(properties.getJwt().getIssuer());
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience().contains(properties.getJwt().getAudience())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "잘못된 audience입니다.", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuer, audience, accountValidator));
        return decoder;
    }

    @Bean("accountJwtAuthenticationConverter")
    Converter<Jwt, AbstractAuthenticationToken> accountJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null) {
                return List.of();
            }
            if ("SELLER".equals(role)) {
                return List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_SELLER"));
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    Clock accountClock() {
        return Clock.systemUTC();
    }
}
