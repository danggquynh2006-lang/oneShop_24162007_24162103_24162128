package com.oneshop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNPAID;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    @Column(unique = true)
    private String reference;
    private String requestDate;
    private String transactionCode;
    private LocalDateTime paidAt;
    private LocalDateTime refundRequestedAt;
}
