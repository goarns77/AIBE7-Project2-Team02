package org.example.matcheat.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.account")
public class AccountProperties {
    private String passwordEncoder = "bcrypt";
    private final Jwt jwt = new Jwt();
    private final AdminBootstrap adminBootstrap = new AdminBootstrap();

    public String getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(String passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public AdminBootstrap getAdminBootstrap() {
        return adminBootstrap;
    }

    public static class Jwt {
        private String secret;
        private String issuer = "https://matcheat.local";
        private String audience = "matcheat-api";
        private Duration accessTokenTtl = Duration.ofHours(1);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }
    }

    public static class AdminBootstrap {
        private boolean enabled;
        private String email;
        private String password;
        private String name;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
