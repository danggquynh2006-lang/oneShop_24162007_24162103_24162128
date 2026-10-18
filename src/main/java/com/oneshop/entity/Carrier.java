package com.oneshop.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carriers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal shippingFee;

    @Builder.Default
    private boolean active = true;
}