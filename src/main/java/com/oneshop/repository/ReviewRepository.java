package com.oneshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneshop.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Đánh giá của một sản phẩm (trang chi tiết). */
    @EntityGraph(attributePaths = "user")
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /** Đánh giá của mọi sản phẩm thuộc một shop (Vendor xem). */
    @EntityGraph(attributePaths = {"user", "product"})
    Page<Review> findByProductShopId(Long shopId, Pageable pageable);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double averageRating(@Param("productId") Long productId);
}