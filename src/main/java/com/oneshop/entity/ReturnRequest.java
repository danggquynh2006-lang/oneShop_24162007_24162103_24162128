package com.oneshop.entity;

import java.time.LocalDateTime;
import com.oneshop.common.ReturnStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private String reason;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status;

    @Column(length = 500)
    private String responseNote;

    private LocalDateTime respondedAt;

    private LocalDateTime createdAt;

    public ReturnStatus getEffectiveStatus() {
        return status == null ? ReturnStatus.PENDING : status;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}