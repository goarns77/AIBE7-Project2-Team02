package org.example.matcheat.domain.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.matcheat.domain.account.enums.SellerVerificationStatus;

public record AdminSellerReviewRequest(
        @NotNull SellerVerificationStatus status,
        @Size(max = 500) String rejectionReason) {
}
