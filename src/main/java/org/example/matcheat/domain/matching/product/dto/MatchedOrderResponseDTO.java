package org.example.matcheat.domain.matching.product.dto;

import lombok.Getter;
import org.example.matcheat.domain.order.entity.OrderRequest;
import org.example.matcheat.domain.order.enums.BudgetType;
import org.example.matcheat.domain.order.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Getter
/**
 * 판매자에게 추천되는 주문 요청 한 건을, 매칭 점수 및 근거와 함께 담는 응답 DTO이다.
 */
public class MatchedOrderResponseDTO {

    /** 추천되는 주문 요청 정보(구매자 계정 ID는 빠져있음) */
    private final RecommendedOrderRequestDTO orderRequest;
    /** 이 판매 조건과 이 주문 요청 간의 종합 매칭 점수(0~100) */
    private final double totalScore;
    /** 종합 점수의 항목별 세부 내역 */
    private final List<RecommendationScoreItemDTO> scoreItems;

    /**
     * 주문 요청 Entity와 매칭 점수 산출 결과를 옮겨 담는 생성자이다.
     */
    private MatchedOrderResponseDTO(OrderRequest orderRequest, MatchScoreResult matchScoreResult) {
        this.orderRequest = RecommendedOrderRequestDTO.from(orderRequest);
        this.totalScore = matchScoreResult.getTotalScore();
        this.scoreItems = matchScoreResult.getBreakdown().stream()
                .map(RecommendationScoreItemDTO::from)
                .toList();
    }

    /**
     * 주문 요청 Entity와 매칭 점수 산출 결과를 응답 DTO로 변환한다.
     */
    public static MatchedOrderResponseDTO of(OrderRequest orderRequest, MatchScoreResult matchScoreResult) {
        return new MatchedOrderResponseDTO(orderRequest, matchScoreResult);
    }

    /**
     * 추천 결과에 담기는 주문 요청 정보이다. domain/order의 OrderRequestResponseDTO는
     * 구매자 계정 ID(buyerId)를 그대로 노출하는데, 이 값이 판매자에게 그대로 보이면 안 되므로
     * 여기서는 buyerId를 뺀 값만 담는 전용 DTO를 별도로 둔다.
     */
    @Getter
    public static class RecommendedOrderRequestDTO {
        /** 주문 요청 ID */
        private final Long id;
        /** 주문 요청 제목 */
        private final String title;
        /** 주문 요청 상세 설명 */
        private final String description;
        /** 행사/이용 일시 */
        private final LocalDateTime eventDateTime;
        /** 주문 수량(인분 수) */
        private final Integer quantity;
        /** 예산 유형(1인당/총액) */
        private final BudgetType budgetType;
        /** 예산 금액 */
        private final BigDecimal budget;
        /** 카테고리 */
        private final String category;
        /** 배송 주소 */
        private final String deliveryAddress;
        /** 배송 주소를 지오코딩한 위도 */
        private final Double latitude;
        /** 배송 주소를 지오코딩한 경도 */
        private final Double longitude;
        /** 주문 요청 상태 */
        private final RequestStatus status;

        /**
         * OrderRequest 엔티티에서 buyerId를 제외한 값만 옮겨 담는 생성자이다.
         */
        private RecommendedOrderRequestDTO(OrderRequest orderRequest) {
            this.id = orderRequest.getId();
            this.title = orderRequest.getTitle();
            this.description = orderRequest.getDescription();
            this.eventDateTime = orderRequest.getEventDateTime();
            this.quantity = orderRequest.getQuantity();
            this.budgetType = orderRequest.getBudgetType();
            this.budget = orderRequest.getBudget();
            this.category = orderRequest.getCategory();
            this.deliveryAddress = orderRequest.getDeliveryAddress();
            this.latitude = orderRequest.getLatitude();
            this.longitude = orderRequest.getLongitude();
            this.status = orderRequest.getStatus();
        }

        /**
         * OrderRequest 엔티티를 buyerId가 빠진 추천용 DTO로 변환한다.
         */
        public static RecommendedOrderRequestDTO from(OrderRequest orderRequest) {
            return new RecommendedOrderRequestDTO(orderRequest);
        }
    }

    /**
     * 매칭 점수 항목 하나(예: 거리 인접도, 예산 적합도)를 응답용으로 담는 DTO이다.
     */
    @Getter
    public static class RecommendationScoreItemDTO {
        /** 이 항목을 코드 레벨에서 식별하기 위한 태그 */
        private final ScoreTag reasonTag;
        /** 화면에 보여줄 항목 이름 */
        private final String label;
        /** 이 항목의 원점수(0~100) */
        private final double score;
        /** 이 계산에서 실제로 적용된 가중치(재분배 반영) */
        private final double weight;
        /** 이 항목이 총점에 기여한 값 */
        private final double contribution;
        /** 화면에 보여줄 근거 문구 */
        private final String reason;

        /**
         * 항목 하나의 점수 정보를 옮겨 담는 생성자이다.
         */
        private RecommendationScoreItemDTO(
                ScoreTag reasonTag,
                String label,
                double score,
                double weight,
                double contribution,
                String reason
        ) {
            this.reasonTag = reasonTag;
            this.label = label;
            this.score = score;
            this.weight = weight;
            this.contribution = contribution;
            this.reason = reason;
        }

        /**
         * ScoreBreakdownItem을 응답용 DTO로 변환한다. label로부터 reasonTag를 유추한다.
         */
        public static RecommendationScoreItemDTO from(ScoreBreakdownItem item) {
            return new RecommendationScoreItemDTO(
                    ScoreTag.fromLabel(item.getLabel()),
                    item.getLabel(),
                    item.getRawScore(),
                    item.getWeight(),
                    item.contribution(),
                    item.getReason()
            );
        }
    }

    /**
     * 매칭 점수 항목을 코드 레벨에서 식별하기 위한 태그이다.
     */
    public enum ScoreTag {
        DISTANCE,
        BUDGET,
        CATEGORY,
        RATING,
        TEXT_SIMILARITY,
        UNKNOWN;

        /**
         * 한글 항목 라벨(예: "거리 인접도")을 보고 알맞은 태그를 찾는다.
         * 알려지지 않은 라벨이면 UNKNOWN을 반환한다.
         */
        public static ScoreTag fromLabel(String label) {
            if (label == null) {
                return UNKNOWN;
            }

            String normalized = label.trim().toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "거리 인접도" -> DISTANCE;
                case "예산 적합도" -> BUDGET;
                case "카테고리 일치도" -> CATEGORY;
                case "판매자 평점" -> RATING;
                case "텍스트 유사도" -> TEXT_SIMILARITY;
                default -> UNKNOWN;
            };
        }
    }
}
