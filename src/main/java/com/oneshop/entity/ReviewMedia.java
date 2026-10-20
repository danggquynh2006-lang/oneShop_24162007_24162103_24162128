package com.oneshop.entity;

import com.oneshop.common.MediaType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_media")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 500)
    private String url;

    /** public_id của Cloudinary để xóa file sau này. */
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;
}