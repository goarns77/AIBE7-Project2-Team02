package org.example.matcheat.domain.matching.filter;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * 외부 경로 조회가 필요하지 않은 주문/판매 조건의 필수 매칭 조건을 판정한다.
 */
@Component
public class HardFilter {

    /**
     * 주문 수량이 판매자의 최소/최대 수용 범위에 포함되는지 확인한다.
     */
    public boolean matchesQuantity(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        Integer quantity = order.getQuantity();

        return product.getMinHeadcount() <= quantity
                && quantity <= product.getMaxHeadcount();
    }

    /**
     * 주문 음식 카테고리와 판매 카테고리가 일치하는지 확인한다.
     */
    public boolean matchesCategory(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return order.getCategory()
                .equalsIgnoreCase(product.getCategory());
    }

    /**
     * 주문 예산으로 해당 상품을 주문할 수 있는지 확인한다.
     */
    public boolean matchesBudget(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        BigDecimal unitPrice =
                BigDecimal.valueOf(product.getServingPrice());

        return switch (order.getBudgetType()) {
            case PER_PERSON -> order.getBudget().compareTo(unitPrice) >= 0;

            case TOTAL -> {
                BigDecimal totalPrice =
                        unitPrice.multiply(
                                BigDecimal.valueOf(
                                        order.getQuantity()
                                )
                        );

                yield order.getBudget()
                        .compareTo(totalPrice) >= 0;
            }
        };
    }

    /**
     * 주문 행사 날짜가 판매 불가 날짜와 겹치지 않는지 확인한다.
     */
    public boolean matchesAvailableDate(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        LocalDate eventDate =
                order.getEventDateTime().toLocalDate();

        return product.getUnavailableDates() == null
                || !product.getUnavailableDates()
                .contains(eventDate);
    }

    /**
     * 숨김 처리되지 않은 판매 조건인지 확인한다.
     */
    public boolean matchesVisible(
            ProductResponseDTO product
    ) {
        return !product.isHidden();
    }

    /**
     * 주문 행사 요일이 정기 휴무일과 겹치지 않는지 확인한다.
     */
    public boolean matchesOperatingDay(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        DayOfWeek closingDay =
                product.getDayOfWeek();

        if (closingDay == null) {
            return true;
        }

        return order.getEventDateTime()
                .getDayOfWeek() != closingDay;
    }

    /**
     * 도로 거리 검사를 제외한 로컬 하드 필터 조건을 모두 확인한다.
     */
    public boolean matchesLocal(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return matchesVisible(product)
                && matchesQuantity(order, product)
                && matchesCategory(order, product)
                && matchesBudget(order, product)
                && matchesAvailableDate(order, product)
                && matchesOperatingDay(order, product);
    }

    /**
     * 기존 호출부와의 호환을 위해 로컬 하드 필터 결과를 반환한다.
     */
    public boolean matches(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return matchesLocal(order, product);
    }
}