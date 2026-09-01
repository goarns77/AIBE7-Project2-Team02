package org.example.matcheat.domain.account.dto;

import org.example.matcheat.domain.account.repository.AdminAccountRepository;

import java.util.List;
import java.util.function.Function;

public record AdminPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public static <S, T> AdminPageResponse<T> from(
            AdminAccountRepository.PageResult<S> result,
            Function<S, T> mapper) {
        return new AdminPageResponse<>(
                result.content().stream().map(mapper).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
