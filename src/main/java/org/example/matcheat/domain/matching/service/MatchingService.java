package org.example.matcheat.domain.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.matching.filter.HardFilter;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.order.service.OrderRequestService;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.example.matcheat.domain.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 주문과 판매 조건을 조회하고 매칭 가능한 후보를 찾는 Service
 */
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final OrderRequestService orderRequestService;
    private final ProductService productService;
    private final HardFilter hardFilter;

    /**
     * 특정 주문 조건에 맞는 판매 조건 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findProductsForOrder(Long orderRequestId) {
        OrderRequestResponseDTO order =
                orderRequestService.findById(orderRequestId);

        return productService.findAll().stream()
                .filter(product -> hardFilter.matches(order, product))
                .toList();
    }
}
