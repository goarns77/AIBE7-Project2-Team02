package org.example.matcheat.domain.matching.filter;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 주문 조건과 판매 조건을 비교해 하드 필터 통과 여부를 판단하는 컴포넌트
 */
@Component
public class HardFilter {
    /**
     * 주문 수량이 판매자의 최소/최대 수용 인원 범위에 포함되는지 확인
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
     * 주문 음식 카테고리와 판매 카테고리가 일치하는지 확인
     */
    public boolean matchesCategory(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return order.getCategory().equalsIgnoreCase(product.getCategory());
    }

    /**
     * 주문의 총예산이 판매자의 최소 주문 금액 이상인지 확인
     */
    public boolean matchesMinOrderAmount(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        BigDecimal totalBudget =
                switch (order.getBudgetType()) {
                    case TOTAL -> order.getBudget();
                    case PER_PERSON -> order.getBudget().multiply(BigDecimal.valueOf(order.getQuantity()));
                };

        BigDecimal servicePrice =
                BigDecimal.valueOf(product.getServingPrice());

        return totalBudget.compareTo(servicePrice) >= 0;
    }

    /**
     * 주문 조건이 모든 하드 필터 조건을 만족하는지 확인
     */
    public boolean matches(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return matchesQuantity(order, product)
                && matchesCategory(order, product)
                && matchesMinOrderAmount(order, product);
    }
}
