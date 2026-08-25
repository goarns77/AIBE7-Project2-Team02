package org.example.matcheat.account.port.in;

public interface CheckEmailAvailabilityUseCase {
    EmailAvailability check(String email);

    record EmailAvailability(String email, boolean available) {
    }
}
