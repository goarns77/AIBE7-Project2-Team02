package org.example.matcheat.domain.account.entity;

public record IssuedAccessToken(String value, long expiresInSeconds) {
}
