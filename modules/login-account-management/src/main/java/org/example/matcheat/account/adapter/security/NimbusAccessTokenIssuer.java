package org.example.matcheat.account.adapter.security;

import org.example.matcheat.account.config.AccountProperties;
import org.example.matcheat.account.domain.IssuedAccessToken;
import org.example.matcheat.account.domain.UserAccount;
import org.example.matcheat.account.port.out.AccessTokenIssuer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class NimbusAccessTokenIssuer implements AccessTokenIssuer {
    private final JwtEncoder encoder;
    private final AccountProperties properties;
    private final Clock clock;

    public NimbusAccessTokenIssuer(
            @Qualifier("accountJwtEncoder") JwtEncoder encoder,
            AccountProperties properties,
            @Qualifier("accountClock") Clock accountClock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = accountClock;
    }

    @Override
    public IssuedAccessToken issue(UserAccount account) {
        if (account.id() == null) {
            throw new IllegalArgumentException("저장되지 않은 사용자에게 토큰을 발급할 수 없습니다.");
        }

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.getJwt().getAccessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(account.id().toString())
                .audience(List.of(properties.getJwt().getAudience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("role", account.role().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(token, properties.getJwt().getAccessTokenTtl().toSeconds());
    }
}
