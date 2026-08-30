package org.example.matcheat.domain.account.service;

import java.util.Locale;
import java.util.regex.Pattern;

public final class EmailNormalizer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    private EmailNormalizer() {
    }

    public static String normalize(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new AccountApplicationException(AccountErrorCode.INVALID_EMAIL, "올바른 이메일 형식이 아닙니다.");
        }
        return normalized;
    }
}
