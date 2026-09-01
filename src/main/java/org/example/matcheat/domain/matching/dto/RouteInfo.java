package org.example.matcheat.domain.matching.dto;

/**
 * 판매 상품 위치에서 주문 배송지까지의 자동차 이동 경로 요약 정보이다.
 */
public record RouteInfo(
        Long productId,
        int distanceMeters,
        int durationSeconds
) {

    /**
     * 도로 이동거리를 km 단위로 반환한다.
     */
    public double distanceKm() {
        return distanceMeters / 1000.0;
    }

    /**
     * 예상 이동시간을 분 단위로 올림하여 반환한다.
     */
    public int durationMinutes() {
        return (int) Math.ceil(
                durationSeconds / 60.0
        );
    }
}