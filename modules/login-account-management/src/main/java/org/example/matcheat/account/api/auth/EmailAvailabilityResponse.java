package org.example.matcheat.account.api.auth;

import org.example.matcheat.account.port.in.CheckEmailAvailabilityUseCase;

public record EmailAvailabilityResponse(String email, boolean available) {
    static EmailAvailabilityResponse from(CheckEmailAvailabilityUseCase.EmailAvailability availability) {
        return new EmailAvailabilityResponse(availability.email(), availability.available());
    }
}
