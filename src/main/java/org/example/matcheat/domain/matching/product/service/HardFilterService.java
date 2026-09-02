package org.example.matcheat.domain.matching.product.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.order.enums.RequestStatus;
import org.example.matcheat.domain.order.repository.OrderRequestRepository;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.example.matcheat.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
/**
 * 판매자의 판매 조건을 기준으로 주문 요청을 1차 하드 필터링하는 서비스이다.
 * 날짜/수량/예산/카테고리 등 필수 조건을 만족하지 못하는 주문 요청은 후보군에서 제외한다.
 */
public class HardFilterService {

    private final ProductRepository productRepository;
    private final OrderRequestRepository orderRequestRepository;

    /**
     * 특정 판매자의 판매 조건을 기준으로 하드 필터를 통과한 주문 요청 후보 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<OrderRequest> findCandidates(Long productId) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(productId)
                ));

        return orderRequestRepository.findAll().stream()
                .filter(orderRequest -> orderRequest.getStatus() == RequestStatus.MATCHING)
                .filter(orderRequest -> matchesHeadcount(product, orderRequest))
                .filter(orderRequest -> matchesBudget(product, orderRequest))
                .filter(orderRequest -> matchesSchedule(product, orderRequest))
                .filter(orderRequest -> matchesCategoryLoosely(product, orderRequest))
                .toList();
    }

    /**
     * 주문 수량(quantity)이 판매자의 min~maxHeadcount 범위 안에 있는지 확인한다.
     */
    private boolean matchesHeadcount(ProductEntity product, OrderRequest orderRequest) {
        Integer quantity = orderRequest.getQuantity();
        return quantity != null
                && quantity >= product.getMinHeadcount()
                && quantity <= product.getMaxHeadcount();
    }

    /**
     * 주문 예산(budget)이 필요 금액(1인분 가격 × budgetType에 따른 산정) 이상인지 확인한다.
     * budgetType이 PER_PERSON이면 1인분 가격 그대로, TOTAL이면 1인분 가격 × 수량을 필요 금액으로 본다.
     * (MatchScoreCalculator.requiredAmount()와 동일한 규칙)
     */
    private boolean matchesBudget(ProductEntity product, OrderRequest orderRequest) {
        if (orderRequest.getBudget() == null) {
            return false;
        }

        return orderRequest.getBudget().doubleValue() >= requiredAmount(product, orderRequest);
    }

    /**
     * budgetType에 따라 실제로 필요한 금액을 계산한다.
     * PER_PERSON: 1인분 가격 그대로. TOTAL: 1인분 가격 × 주문 수량.
     */
    private double requiredAmount(ProductEntity product, OrderRequest orderRequest) {
        double servingPrice = product.getServingPrice();

        if (orderRequest.getBudgetType() == null) {
            return servingPrice;
        }

        return switch (orderRequest.getBudgetType()) {
            case PER_PERSON -> servingPrice;
            case TOTAL -> servingPrice * (orderRequest.getQuantity() != null ? orderRequest.getQuantity() : 1);
        };
    }

    /**
     * 행사 요일이 판매자의 정기 휴무 요일(dayOfWeek)과 겹치지 않고,
     * 행사 날짜가 판매자의 개별 휴무일(unavailableDates)에 포함되지 않는지 확인한다.
     */
    private boolean matchesSchedule(ProductEntity product, OrderRequest orderRequest) {
        if (orderRequest.getEventDateTime() == null) {
            return false;
        }

        DayOfWeek eventDayOfWeek = orderRequest.getEventDateTime().getDayOfWeek();
        if (product.getDayOfWeek() != null && product.getDayOfWeek() == eventDayOfWeek) {
            return false;
        }

        if (product.getUnavailableDates() != null
                && product.getUnavailableDates().contains(orderRequest.getEventDateTime().toLocalDate())) {
            return false;
        }

        return true;
    }

    /**
     * 카테고리는 완전히 무관한 경우만 하드 필터에서 걸러내고,
     * 세부 일치도 평가는 소프트 매칭 점수 단계에서 처리한다.
     */
    private boolean matchesCategoryLoosely(ProductEntity product, OrderRequest orderRequest) {
        if (product.getCategory() == null || orderRequest.getCategory() == null) {
            return true;
        }

        String productCategory = product.getCategory().toLowerCase(Locale.ROOT);
        String orderCategory = orderRequest.getCategory().toLowerCase(Locale.ROOT);

        return productCategory.contains(orderCategory) || orderCategory.contains(productCategory);
    }
}
