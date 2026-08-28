package org.example.matcheat.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SellerApplicationRequest(
        @NotBlank(message = "사업자명을 입력해 주세요.")
        @Size(max = 100, message = "사업자명은 100자 이하여야 합니다.")
        String businessName,

        @NotBlank(message = "사업자번호를 입력해 주세요.")
        @Size(max = 20, message = "사업자번호는 20자 이하여야 합니다.")
        String businessNumber,

        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal deliveryRadiusKm) {
}
