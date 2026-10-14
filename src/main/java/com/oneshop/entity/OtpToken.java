package com.oneshop.entity;

import java.time.LocalDateTime;
import com.oneshop.common.OtpType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "otp_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    private boolean used;
    private int attempts;
}