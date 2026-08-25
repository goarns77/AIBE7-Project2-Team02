package org.example.matcheat.account.port.in;

import org.example.matcheat.account.domain.SellerVerificationStatus;
import org.example.matcheat.account.domain.UserStatus;

public interface AdminAccountManagementUseCase {
    void changeUserStatus(long adminId, long userId, UserStatus status);

    void reviewSeller(
            long adminId,
            long sellerId,
            SellerVerificationStatus status,
            String rejectionReason);
}
