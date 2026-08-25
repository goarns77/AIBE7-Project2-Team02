package org.example.matcheat.account.port.in;

import org.example.matcheat.account.domain.AccountProfile;

public interface ProfileUseCase {
    AccountProfile getCurrentUser(long userId);

    AccountProfile updateName(long userId, String name);

    void withdraw(long userId, String currentPassword);
}
