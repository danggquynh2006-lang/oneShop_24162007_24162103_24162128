package com.oneshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);
    Page<Product> findByShopIdAndActiveTrue(Long shopId, Pageable pageable);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    /** Dùng cho trợ lý làm đẹp (BeautyAssistantService): sản phẩm đang bán, bán chạy nhất trước. */
    @Query("select product from Product product join fetch product.shop where product.active = true "
            + "and product.shop.status = :status order by product.sold desc, product.id desc")
    List<Product> findFeatured(@Param("status") ShopStatus status, Pageable pageable);
}
