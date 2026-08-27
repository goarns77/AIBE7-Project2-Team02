package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.enums.UserRole;
import org.example.matcheat.domain.account.enums.UserStatus;
import org.example.matcheat.domain.account.service.AccountProfileService;

public record AccountProfileResponse(
        Long userId,
        String email,
        String name,
        UserRole role,
        UserStatus status,
        SellerVerificationStatus sellerStatus) {
    public static AccountProfileResponse from(AccountProfileService.ProfileResult result) {
        return new AccountProfileResponse(
                result.userId(),
                result.email(),
                result.name(),
                result.role(),
                result.status(),
                result.sellerStatus());
    }
}
