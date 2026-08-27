package org.example.matcheat.domain.account.security;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
