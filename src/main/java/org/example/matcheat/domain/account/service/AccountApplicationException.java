package org.example.matcheat.domain.account.service;

public final class AccountApplicationException extends RuntimeException {
    private final AccountErrorCode code;

    public AccountApplicationException(AccountErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public AccountErrorCode code() {
        return code;
    }
}
