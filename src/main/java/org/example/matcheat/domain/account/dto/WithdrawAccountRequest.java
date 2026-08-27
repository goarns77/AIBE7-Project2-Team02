package org.example.matcheat.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WithdrawAccountRequest(
        @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
        @Size(max = 64, message = "비밀번호는 64자 이하여야 합니다.")
        String currentPassword) {
}
