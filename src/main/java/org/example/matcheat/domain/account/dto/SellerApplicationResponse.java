package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.enums.SellerVerificationStatus;
import org.example.matcheat.domain.account.service.SellerApplicationService;

public record SellerApplicationResponse(Long sellerId, SellerVerificationStatus status) {
    public static SellerApplicationResponse from(SellerApplicationService.ApplicationResult result) {
        return new SellerApplicationResponse(result.sellerId(), result.status());
    }
}
