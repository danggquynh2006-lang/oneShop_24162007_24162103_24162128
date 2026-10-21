package com.oneshop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;
    @Column(length = 4000)
    private String description;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    @Builder.Default
    private int stock = 0;
    @Builder.Default
    private int sold = 0;
    @Builder.Default
    private double avgRating = 0;
    private String brand;
    @Builder.Default
    private boolean active = true;

    /**
     * Admin/Manager khóa sản phẩm. Khi khóa thì active = false và Vendor không bật lại được.
     * Dùng Boolean để dữ liệu cũ (null) vẫn hợp lệ, hiểu là chưa khóa.
     */
    @Builder.Default
    private Boolean locked = false;

    private LocalDateTime createdAt;

    public boolean isLocked() {
        return Boolean.TRUE.equals(locked);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}