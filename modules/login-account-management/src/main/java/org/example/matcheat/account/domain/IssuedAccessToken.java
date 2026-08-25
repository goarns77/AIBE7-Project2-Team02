package org.example.matcheat.account.domain;

public record IssuedAccessToken(String value, long expiresInSeconds) {
}
