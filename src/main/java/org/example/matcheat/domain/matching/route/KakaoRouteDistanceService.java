package org.example.matcheat.domain.matching.route;

import org.example.matcheat.domain.matching.dto.RouteInfo;
import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

import java.util.*;

/**
 * Kakao Mobility 다중 출발지 길찾기 API로 실제 자동차 이동거리를 조회한다.
 */
@Service
public class KakaoRouteDistanceService
        implements RouteDistanceService {

    private static final int MAX_ORIGINS = 30;
    private static final int ROUTE_SEARCH_RADIUS_METERS = 10_000;

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoRouteDistanceService(
            RestClient.Builder restClientBuilder,
            @Value("${app.kakao.rest-api-key:}")
            String restApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(
                        "https://apis-navi.kakaomobility.com"
                )
                .build();

        this.restApiKey = restApiKey;
    }

    /**
     * 상품을 최대 30개씩 나누어 주문 배송지까지의 도로 경로를 조회한다.
     */
    @Override
    public Map<Long, RouteInfo> findRoutes(
            OrderRequestResponseDTO order,
            List<ProductResponseDTO> products
    ) {
        if (products == null || products.isEmpty()) {
            return Map.of();
        }

        validateApiKey();
        validateOrderCoordinates(order);

        Map<Long, RouteInfo> result =
                new HashMap<>();

        List<ProductResponseDTO> routableProducts =
                products.stream()
                        .filter(this::hasCoordinates)
                        .toList();

        for (int start = 0;
             start < routableProducts.size();
             start += MAX_ORIGINS) {

            int end = Math.min(
                    start + MAX_ORIGINS,
                    routableProducts.size()
            );

            List<ProductResponseDTO> batch =
                    routableProducts.subList(
                            start,
                            end
                    );

            result.putAll(
                    requestRoutes(order, batch)
            );
        }

        return result;
    }

    /**
     * 한 번의 다중 출발지 요청을 Kakao Mobility에 전송한다.
     */
    private Map<Long, RouteInfo> requestRoutes(
            OrderRequestResponseDTO order,
            List<ProductResponseDTO> products
    ) {
        List<Map<String, Object>> origins =
                new ArrayList<>();

        for (ProductResponseDTO product : products) {
            Map<String, Object> origin =
                    new LinkedHashMap<>();

            // Kakao API에서 x는 경도, y는 위도이다.
            origin.put("x", product.getLongitude());
            origin.put("y", product.getLatitude());
            origin.put(
                    "key",
                    String.valueOf(product.getId())
            );

            origins.add(origin);
        }

        Map<String, Object> destination =
                new LinkedHashMap<>();

        destination.put(
                "x",
                order.getLongitude()
        );
        destination.put(
                "y",
                order.getLatitude()
        );

        Map<String, Object> requestBody =
                new LinkedHashMap<>();

        requestBody.put("origins", origins);
        requestBody.put(
                "destination",
                destination
        );

        // Kakao 길찾기의 경로 탐색 반경이며 판매자의 배송 가능 반경과는 다른 값이다.
        requestBody.put(
                "radius",
                ROUTE_SEARCH_RADIUS_METERS
        );

        // 판매자가 거리 기준 배송 범위를 설정하므로 최단 거리 경로를 사용한다.
        requestBody.put(
                "priority",
                "DISTANCE"
        );

        try {
            JsonNode response =
                    restClient.post()
                            .uri(
                                    "/v1/origins/directions"
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "KakaoAK " + restApiKey
                            )
                            .header(
                                    HttpHeaders.CONTENT_TYPE,
                                    "application/json"
                            )
                            .body(requestBody)
                            .retrieve()
                            .body(JsonNode.class);

            return parseRoutes(response);

        } catch (RestClientResponseException exception) {
            throw new IllegalStateException(
                    "도로 이동거리 조회에 실패했습니다.",
                    exception
            );
        }
    }

    /**
     * Kakao Mobility 응답을 상품 ID별 경로 정보로 변환한다.
     */
    private Map<Long, RouteInfo> parseRoutes(
            JsonNode response
    ) {
        if (response == null
                || !response.has("routes")) {

            throw new IllegalStateException(
                    "도로 이동거리 응답 형식이 올바르지 않습니다."
            );
        }

        Map<Long, RouteInfo> result =
                new HashMap<>();

        for (JsonNode route :
                response.path("routes")) {

            // 일부 출발지만 길찾기에 실패할 수 있으므로 성공 경로만 사용한다.
            if (route.path("result_code")
                    .asInt(-1) != 0) {
                continue;
            }

            String key =
                    route.path("key").asText();

            JsonNode summary =
                    route.path("summary");

            if (key.isBlank()
                    || summary.isMissingNode()) {
                continue;
            }

            Long productId =
                    Long.valueOf(key);

            int distanceMeters =
                    summary.path("distance")
                            .asInt();

            int durationSeconds =
                    summary.path("duration")
                            .asInt();

            result.put(
                    productId,
                    new RouteInfo(
                            productId,
                            distanceMeters,
                            durationSeconds
                    )
            );
        }

        return result;
    }

    private boolean hasCoordinates(
            ProductResponseDTO product
    ) {
        return product.getId() != null
                && product.getLatitude() != null
                && product.getLongitude() != null;
    }

    private void validateOrderCoordinates(
            OrderRequestResponseDTO order
    ) {
        if (order.getLatitude() == null
                || order.getLongitude() == null) {

            throw new IllegalStateException(
                    "주문 배송지 좌표가 없습니다."
            );
        }
    }

    private void validateApiKey() {
        if (restApiKey == null
                || restApiKey.isBlank()) {

            throw new IllegalStateException(
                    "KAKAO_REST_API_KEY가 설정되지 않았습니다."
            );
        }
    }
}