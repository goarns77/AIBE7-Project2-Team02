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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_headcount", nullable = false)
    private Integer minHeadcount;

    @Column(name = "max_headcount", nullable = false)
    private Integer maxHeadcount;

    @Column(name = "min_order_amount", nullable = false)
    private Integer minOrderAmount;

    @Column(name = "delivery_radius_km", nullable = false)
    private Double deliveryRadiusKm;

    @Column(name = "store_address", nullable = false)
    private String storeAddress;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = true)
    private DayOfWeek dayOfWeek;

    @Column(name = "rating_avg", nullable = true)
    @ColumnDefault("0.0")
    private Double ratingAvg;

    @Convert(converter = LocalDateListConverter.class)
    @Column(name = "unavailable_dates", nullable = true, columnDefinition = "TEXT")
    private List<LocalDate> unavailableDates;

    @Column(name = "image_url", nullable = true, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 정적 팩토리 메소드를 통해서만 생성되도록 하는 생성자이다.
     */
    private ProductEntity(
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer minOrderAmount,
            Double deliveryRadiusKm,
            String storeAddress,
            String category,
            String description,
            DayOfWeek dayOfWeek,
            List<LocalDate> unavailableDates,
            String imageUrl
    ) {
        this.minHeadcount = minHeadcount;
        this.maxHeadcount = maxHeadcount;
        this.minOrderAmount = minOrderAmount;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.storeAddress = storeAddress;
        this.category = category;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.unavailableDates = normalizeUnavailableDates(unavailableDates);
        this.imageUrl = imageUrl;
    }

    /**
     * 새 판매 조건 엔티티를 생성한다.
     */
    public static ProductEntity create(
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer minOrderAmount,
            Double deliveryRadiusKm,
            String storeAddress,
            String category,
            String description,
            DayOfWeek dayOfWeek,
            List<LocalDate> unavailableDates,
            String imageUrl
    ) {
        return new ProductEntity(
                minHeadcount,
                maxHeadcount,
                minOrderAmount,
                deliveryRadiusKm,
                storeAddress,
                category,
                description,
                dayOfWeek,
                unavailableDates,
                imageUrl
        );
    }

    /**
     * null 이 아닌 값만 반영해 판매 조건을 부분 수정한다.
     */
    public void update(
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer minOrderAmount,
            Double deliveryRadiusKm,
            String storeAddress,
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

        if(minOrderAmount != null) {
            this.minOrderAmount = minOrderAmount;
        }

        if(deliveryRadiusKm != null) {
            this.deliveryRadiusKm = deliveryRadiusKm;
        }

        if(storeAddress != null) {
            this.storeAddress = storeAddress;
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
     * 엔티티가 처음 저장될 때 수정 시각을 현재 시각으로 초기화한다.
     */
    @PrePersist
    protected void onCreate() {
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
