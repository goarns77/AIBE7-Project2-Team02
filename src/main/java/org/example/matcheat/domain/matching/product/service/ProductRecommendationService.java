package org.example.matcheat.domain.matching.product.service;

import lombok.RequiredArgsConstructor;
import org.example.matcheat.domain.matching.product.dto.MatchedOrderResponseDTO;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.product.entity.ProductEntity;
import org.example.matcheat.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * 판매 조건 1건에 대해 추천 주문 요청 목록을 생성하는 오케스트레이션 서비스이다.
 */
public class ProductRecommendationService {

    private final ProductRepository productRepository;
    private final HardFilterService hardFilterService;
    private final MatchScoreCalculator matchScoreCalculator;

    /**
     * 판매 조건에 대한 추천 주문 요청 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<MatchedOrderResponseDTO> recommend(Long productId) {
        ProductEntity product = productRepository.findByIdAndHiddenFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 판매 조건입니다. id=%s".formatted(productId)
                ));

        List<OrderRequest> candidates = hardFilterService.findCandidates(product.getId());

        return candidates.stream()
                .map(orderRequest -> MatchedOrderResponseDTO.of(
                        orderRequest,
                        matchScoreCalculator.calculate(product, orderRequest)
                ))
                .sorted(Comparator.comparingDouble(MatchedOrderResponseDTO::getTotalScore).reversed())
                .toList();
    }
}
