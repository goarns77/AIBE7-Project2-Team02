package org.example.matcheat.account.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderConfigurationTest {
    @Test
    void storesAlgorithmIdentifierAndMatchesPassword() {
        AccountProperties properties = new AccountProperties();
        AccountSecuritySupportConfiguration configuration = new AccountSecuritySupportConfiguration();
        PasswordEncoder encoder = configuration.accountPasswordEncoder(properties);

        String encoded = encoder.encode("password1234");

        assertThat(encoded).startsWith("{bcrypt}$2");
        assertThat(encoder.matches("password1234", encoded)).isTrue();
        assertThat(encoder.matches("wrong1234", encoded)).isFalse();
    }
}
