package org.example.matcheat.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seller_conditions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 판매 조건 정보를 DB에 저장하는 JPA 엔티티이다.
 */
public class ProductEntity {

    // Id
    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 최소 수주(주문) 수량
     */
    @Column(name = "min_headcount", nullable = false)
    private Integer minHeadcount;

    /**
     * 최대 수주(주문) 수량
     */
    @Column(name = "max_headcount", nullable = false)
    private Integer maxHeadcount;

    /**
     *  1인분 가격
     */
    @Column(name = "servingPrice", nullable = false)
    private Integer servingPrice;

    /**
     * 최대 배달(배송) 반경
     */
    @Column(name = "delivery_radius_km", nullable = false)
    private Double deliveryRadiusKm;

    /**
     * 가게 주소
     */
    @Column(name = "store_address", nullable = false)
    private String storeAddress;

    /**
     * 상품/메뉴명
     */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /**
     * 가게 위도
     */
    @Column(name = "latitude", nullable = true)
    private Double latitude;

    /**
     * 가게 경도
     */
    @Column(name = "longitude", nullable = true)
    private Double longitude;

    /**
     * 상품(음식) 카테고리
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * 상품(음식) 설명
     */
    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    /**
     * 가게 정기 휴무 요일
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = true)
    private DayOfWeek dayOfWeek;

    /**
     * 가게 평점
     */
    @Column(name = "rating_avg", nullable = true)
    @ColumnDefault("0.0")
    private Double ratingAvg;

    /**
     * 가게 특정 불가 날짜
     */
    @Convert(converter = LocalDateListConverter.class)
    @Column(name = "unavailable_dates", nullable = true, columnDefinition = "TEXT")
    private List<LocalDate> unavailableDates;

    /**
     * 가게 또는 상품(음식) 이미지
     */
    @Column(name = "image_url", nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * 추후 로그인 구현 시 삭제 예정인 컬럼
     */
    @Column(name = "owner_account_id")
    private Long ownerAccountId;

    /**
     * 상품 Soft Delete 옵션
     */
    @Column(name = "hidden", nullable = false)
    @ColumnDefault("false")
    private boolean hidden;

    /**
     * 데이터 수정 일자 등록
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 정적 팩토리 메소드를 통해서만 생성되도록 하는 생성자이다.
     */
    private ProductEntity(
            String productName,
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer servingPrice,
            Double deliveryRadiusKm,
            String storeAddress,
            Double latitude,
            Double longitude,
            String category,
            String description,
            DayOfWeek dayOfWeek,
            List<LocalDate> unavailableDates,
            String imageUrl,
            Long ownerAccountId
    ) {
        this.minHeadcount = minHeadcount;
        this.maxHeadcount = maxHeadcount;
        this.servingPrice = servingPrice;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.storeAddress = storeAddress;
        this.productName = productName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.unavailableDates = normalizeUnavailableDates(unavailableDates);
        this.imageUrl = imageUrl;
        this.ownerAccountId = ownerAccountId;
        this.hidden = false;
    }

    /**
     * 새 판매 조건 엔티티를 생성한다.
     */
    public static ProductEntity create(
            String productName,
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer servingPrice,
            Double deliveryRadiusKm,
            String storeAddress,
            Double latitude,
            Double longitude,
            String category,
            String description,
            DayOfWeek dayOfWeek,
            List<LocalDate> unavailableDates,
            String imageUrl,
            Long ownerAccountId
    ) {
        return new ProductEntity(
                productName,
                minHeadcount,
                maxHeadcount,
                servingPrice,
                deliveryRadiusKm,
                storeAddress,
                latitude,
                longitude,
                category,
                description,
                dayOfWeek,
                unavailableDates,
                imageUrl,
                ownerAccountId
        );
    }

    /**
     * null 이 아닌 값만 반영해 판매 조건을 부분 수정한다.
     */
    public void update(
            String productName,
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer servingPrice,
            Double deliveryRadiusKm,
            String storeAddress,
            Double latitude,
            Double longitude,
            String category,
            String description,
            DayOfWeek dayOfWeek,
            List<LocalDate> unavailableDates,
            String imageUrl
    ) {
        if(minHeadcount != null) {
            this.minHeadcount = minHeadcount;
        }

        if(maxHeadcount != null) {
            this.maxHeadcount = maxHeadcount;
        }

        if(servingPrice != null) {
            this.servingPrice = servingPrice;
        }

        if(deliveryRadiusKm != null) {
            this.deliveryRadiusKm = deliveryRadiusKm;
        }

        if(storeAddress != null) {
            this.storeAddress = storeAddress;
        }

        if (productName != null) {
            this.productName = productName;
        }

        if(latitude != null) {
            this.latitude = latitude;
        }

        if(longitude != null) {
            this.longitude = longitude;
        }

        if(category != null) {
            this.category = category;
        }

        if (description != null) {
            this.description = description;
        }

        if(dayOfWeek != null) {
            this.dayOfWeek = dayOfWeek;
        }

        if (unavailableDates != null) {
            this.unavailableDates = normalizeUnavailableDates(unavailableDates);
        }

        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    /**
     * 판매 조건을 소프트 삭제 상태로 바꾼다.
     */
    public void softDelete(Long requesterAccountId) {
        // TODO: 로그인 담당 개발이 끝나면 여기서 현재 로그인 사용자의 accountId를 전달받아
        //       ownerAccountId와 일치하는 경우에만 숨김 처리하도록 검증을 강화한다.
        if (this.ownerAccountId != null
                && requesterAccountId != null
                && !this.ownerAccountId.equals(requesterAccountId)) {
            throw new IllegalArgumentException("본인이 등록한 판매 조건만 삭제할 수 있습니다.");
        }

        this.hidden = true;
    }

    /**
     * 엔티티가 처음 저장될 때 수정 시각을 현재 시각으로 초기화한다.
     */
    @PrePersist
    protected void onCreate() {
        this.hidden = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티가 수정될 때 수정 시각을 현재 시각으로 갱신한다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    private List<LocalDate> normalizeUnavailableDates(List<LocalDate> unavailableDates) {
        if (unavailableDates == null || unavailableDates.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(unavailableDates);
    }
}
