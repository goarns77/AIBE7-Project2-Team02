package org.example.matcheat.domain.product.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List&lt;LocalDate&gt;를 콤마로 구분된 문자열 컬럼으로 저장/조회하기 위한 JPA 컨버터이다.
 */
@Converter
public class LocalDateListConverter implements AttributeConverter<List<LocalDate>, String> {

    /**
     * 날짜 목록을 "yyyy-MM-dd,yyyy-MM-dd" 형태의 콤마 구분 문자열로 변환한다.
     * 비어있으면 null을 반환한다.
     */
    @Override
    public String convertToDatabaseColumn(List<LocalDate> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        return attribute.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));
    }

    /**
     * DB에 저장된 콤마 구분 문자열을 다시 List&lt;LocalDate&gt;로 파싱한다.
     * null이거나 빈 문자열이면 빈 리스트를 반환한다.
     */
    @Override
    public List<LocalDate> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }

        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(LocalDate::parse)
                .toList();
    }
}
