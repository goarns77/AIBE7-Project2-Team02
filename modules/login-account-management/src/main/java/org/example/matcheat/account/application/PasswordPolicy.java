package org.example.matcheat.account.application;

public final class PasswordPolicy {
    private PasswordPolicy() {
    }

    public static void validate(String password) {
        if (password == null || password.length() < 8 || password.length() > 64) {
            throw validationError();
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int index = 0; index < password.length(); index++) {
            char character = password.charAt(index);
            if (Character.isISOControl(character)) {
                throw validationError();
            }
            hasLetter |= (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z');
            hasDigit |= character >= '0' && character <= '9';
        }

        if (!hasLetter || !hasDigit) {
            throw validationError();
        }
    }

    private static AccountApplicationException validationError() {
        return new AccountApplicationException(
                AccountErrorCode.VALIDATION_FAILED,
                "비밀번호는 8~64자이며 영문과 숫자를 각각 하나 이상 포함해야 합니다.");
    }
}
