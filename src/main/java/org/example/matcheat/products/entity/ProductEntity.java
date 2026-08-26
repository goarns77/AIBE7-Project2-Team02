package org.example.matcheat.products.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

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

    @Column(name = "rating_avg", nullable = true)
    @ColumnDefault("0.0")
    private Double ratingAvg;

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
            String category
    ) {
        this.minHeadcount = minHeadcount;
        this.maxHeadcount = maxHeadcount;
        this.minOrderAmount = minOrderAmount;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.storeAddress = storeAddress;
        this.category = category;
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
            String category
    ) {
        return new ProductEntity(
                minHeadcount,
                maxHeadcount,
                minOrderAmount,
                deliveryRadiusKm,
                storeAddress,
                category
        );
    }

    /**
     * null 이 아닌 값만 반영해 판매 조건을 부분 수정한다.
     */
    public void update (
            Integer minHeadcount,
            Integer maxHeadcount,
            Integer minOrderAmount,
            Double deliveryRadiusKm,
            String storeAddress,
            String category
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
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
