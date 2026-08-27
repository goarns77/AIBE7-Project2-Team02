package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.service.AccountAuthService;

public record EmailAvailabilityResponse(String email, boolean available) {
    public static EmailAvailabilityResponse from(AccountAuthService.EmailAvailability availability) {
        return new EmailAvailabilityResponse(availability.email(), availability.available());
    }
}
