package org.example.matcheat.domain.matching.filter;

import org.example.matcheat.domain.order.dto.OrderRequestResponseDTO;
import org.example.matcheat.domain.product.dto.ProductResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

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
     * 주문 예산으로 해당 상품의 주문 수량만큼 구매할 수 있는지 확인
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
                                BigDecimal.valueOf(order.getQuantity())
                        );

                yield order.getBudget().compareTo(totalPrice) >= 0;
            }
        };
    }

    /**
     * 주문 행사 날짜가 판매자의 판매 불가 날짜에 포함되지 않는지 확인
     */
    public boolean matchesAvailableDate(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        LocalDate eventDate = order.getEventDateTime().toLocalDate();

        return product.getUnavailableDates() == null
                || !product.getUnavailableDates().contains(eventDate);
    }

    /**
     * 숨김 처리된 판매 조건이 아닌지 확인한다.
     */
    public boolean matchesVisible(ProductResponseDTO product) {
        return !product.isHidden();
    }

    /**
     * 주문 행사 요일이 판매자의 정기 휴무 요일과 겹치지 않는지 확인한다.
     */
    public boolean matchesOperatingDay(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        DayOfWeek closingDay = product.getDayOfWeek();

        // 정기 휴무일을 지정하지 않았다면 모든 요일에 주문 가능하다.
        if (closingDay == null) {
            return true;
        }

        DayOfWeek eventDay =
                order.getEventDateTime().getDayOfWeek();

        return eventDay != closingDay;
    }

    /**
     * 주문 배송지가 판매자의 배송 가능 반경 안에 있는지 확인한다.
     */
    public boolean matchesDeliveryRadius(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        Double orderLatitude = order.getLatitude();
        Double orderLongitude = order.getLongitude();

        Double productLatitude = product.getLatitude();
        Double productLongitude = product.getLongitude();

        Double deliveryRadiusKm =
                product.getDeliveryRadiusKm();

        // 위치를 확인할 수 없는 판매 조건은 배송 가능 여부를 보장할 수 없으므로 제외한다.
        if (orderLatitude == null
                || orderLongitude == null
                || productLatitude == null
                || productLongitude == null
                || deliveryRadiusKm == null
                || deliveryRadiusKm < 0) {

            return false;
        }

        double distanceKm = calculateDistanceKm(
                orderLatitude,
                orderLongitude,
                productLatitude,
                productLongitude
        );

        return distanceKm <= deliveryRadiusKm;
    }

    /**
     * 두 위도/경도 사이의 직선 거리를 Haversine 공식으로 계산한다.
     */
    private double calculateDistanceKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        final double earthRadiusKm = 6371.0088;

        double latitudeDistance =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDistance =
                Math.toRadians(longitude2 - longitude1);

        double firstLatitude =
                Math.toRadians(latitude1);

        double secondLatitude =
                Math.toRadians(latitude2);

        double haversine =
                Math.pow(Math.sin(latitudeDistance / 2), 2)
                        + Math.cos(firstLatitude)
                        * Math.cos(secondLatitude)
                        * Math.pow(
                        Math.sin(longitudeDistance / 2),
                        2
                );

        double centralAngle =
                2 * Math.atan2(
                        Math.sqrt(haversine),
                        Math.sqrt(1 - haversine)
                );

        return earthRadiusKm * centralAngle;
    }

    /**
     * 주문 조건이 모든 하드 필터 조건을 만족하는지 확인한다.
     */
    public boolean matches(
            OrderRequestResponseDTO order,
            ProductResponseDTO product
    ) {
        return matchesVisible(product)
                && matchesQuantity(order, product)
                && matchesCategory(order, product)
                && matchesBudget(order, product)
                && matchesAvailableDate(order, product)
                && matchesOperatingDay(order, product)
                && matchesDeliveryRadius(order, product);
    }
}
