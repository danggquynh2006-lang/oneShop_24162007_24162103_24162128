package com.oneshop.user;

import org.springframework.data.domain.Page;

import com.oneshop.entity.Review;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Dữ liệu để vẽ phần đánh giá trên trang chi tiết sản phẩm (fragments/reviews.html). */
@Getter
@AllArgsConstructor
public class ReviewSection {
    private final Long productId;
    private final Page<Review> reviews;
    private final double average;
    private final long total;
    private final boolean loggedIn;
    /** true = người xem được phép viết đánh giá (đã mua, đơn đã giao, chưa đánh giá). */
    private final boolean canReview;
    /** Lý do chưa được đánh giá (null nếu canReview = true). */
    private final String note;
}