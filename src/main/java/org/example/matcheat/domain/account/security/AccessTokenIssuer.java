package org.example.matcheat.domain.account.security;

import org.example.matcheat.domain.account.entity.IssuedAccessToken;
import org.example.matcheat.domain.account.entity.UserAccount;

public interface AccessTokenIssuer {
    IssuedAccessToken issue(UserAccount account);
}
