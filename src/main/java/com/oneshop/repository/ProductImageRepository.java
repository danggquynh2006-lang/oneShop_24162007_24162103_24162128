package com.oneshop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByMainImageDescIdAsc(Long productId);
    void deleteByProductId(Long productId);
}
