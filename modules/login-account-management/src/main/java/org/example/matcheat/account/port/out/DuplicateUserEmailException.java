package org.example.matcheat.account.port.out;

public final class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(Throwable cause) {
        super(cause);
    }
}
