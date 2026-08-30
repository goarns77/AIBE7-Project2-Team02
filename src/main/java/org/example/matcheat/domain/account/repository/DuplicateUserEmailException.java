package org.example.matcheat.domain.account.repository;

public final class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(Throwable cause) {
        super(cause);
    }
}
