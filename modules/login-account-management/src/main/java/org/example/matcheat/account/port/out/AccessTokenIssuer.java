package org.example.matcheat.account.port.out;

import org.example.matcheat.account.domain.IssuedAccessToken;
import org.example.matcheat.account.domain.UserAccount;

public interface AccessTokenIssuer {
    IssuedAccessToken issue(UserAccount account);
}
