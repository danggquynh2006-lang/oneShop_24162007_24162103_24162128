package com.oneshop.repository;

import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByMainImageDescIdAsc(Long productId);
    List<ProductImage> findByProductIdInOrderByMainImageDescIdAsc(Collection<Long> productIds);
    void deleteByProductId(Long productId);
}
