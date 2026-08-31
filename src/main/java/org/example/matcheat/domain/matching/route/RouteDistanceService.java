package org.example.matcheat.domain.matching.route;

import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;

import java.util.List;
import java.util.Map;

/**
 * 판매 위치에서 주문 배송지까지의 실제 도로 이동거리를 조회한다.
 */
public interface RouteDistanceService {

    /**
     * 여러 판매 위치에서 하나의 주문 배송지까지의 경로 정보를 조회한다.
     */
    Map<Long, RouteInfo> findRoutes(
            OrderRequestResponseDTO order,
            List<ProductResponseDTO> products
    );
}