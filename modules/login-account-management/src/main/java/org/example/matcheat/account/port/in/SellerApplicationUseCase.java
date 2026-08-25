package org.example.matcheat.account.port.in;

import java.math.BigDecimal;

public interface SellerApplicationUseCase {
    long apply(SellerApplicationCommand command);

    record SellerApplicationCommand(
            long userId,
            String businessName,
            String businessNumber,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal deliveryRadiusKm) {
    }
}
