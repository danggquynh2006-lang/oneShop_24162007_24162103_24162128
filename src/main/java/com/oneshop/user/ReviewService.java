package com.oneshop.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.entity.Product;
import com.oneshop.entity.Review;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ReviewRepository;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviews;
    private final ProductRepository products;
    private final UserRepository users;
    private final OrderItemRepository orderItems;

    @Transactional(readOnly = true)
    public List<Review> list(Long productId) {
        return reviews.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public boolean canReview(Long productId, Long userId) {
        return userId != null && !reviews.existsByProductIdAndUserId(productId, userId)
                && orderItems.hasDeliveredPurchase(productId, userId);
    }

    @Transactional
    public Review create(Long productId, Long userId, ReviewForm form) {
        if (!canReview(productId, userId)) {
            throw new BusinessException("Chỉ khách đã nhận sản phẩm mới được đánh giá, mỗi sản phẩm một lần.");
        }
        Product product = products.findById(productId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm."));
        Review review = Review.builder().product(product).user(users.getReferenceById(userId))
                .rating(form.getRating()).comment(form.getComment() == null ? null : form.getComment().trim()).build();
        reviews.saveAndFlush(review);
        product.setAvgRating(reviews.averageRating(productId));
        return review;
    }
}
