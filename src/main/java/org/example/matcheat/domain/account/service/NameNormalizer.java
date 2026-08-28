package org.example.matcheat.domain.account.service;

final class NameNormalizer {
    private NameNormalizer() {
    }

    static String normalize(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isEmpty() || normalized.length() > 50) {
            throw new AccountApplicationException(AccountErrorCode.VALIDATION_FAILED, "이름을 확인해 주세요.");
        }
        return normalized;
    }
}
