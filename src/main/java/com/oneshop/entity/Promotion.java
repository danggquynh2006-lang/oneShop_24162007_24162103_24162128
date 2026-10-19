package com.oneshop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.oneshop.common.PromotionScope;
import com.oneshop.common.PromotionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionScope scope;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal value;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal minOrder;
    @Column(precision = 18, scale = 2)
    private BigDecimal maxDiscount;
    @Column(nullable = false)
    private LocalDateTime startAt;
    @Column(nullable = false)
    private LocalDateTime endAt;
    @Column(nullable = false)
    private int quantity;
    @Builder.Default
    private int used = 0;
    @Builder.Default
    private boolean active = true;
}
