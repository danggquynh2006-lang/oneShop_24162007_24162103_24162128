package com.oneshop.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.oneshop.entity.Product;
import com.oneshop.common.ShopStatus;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByShopIdAndNameContainingIgnoreCase(Long shopId, String name, Pageable pageable);
    Page<Product> findByShopIdAndActiveTrue(Long shopId, Pageable pageable);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);

    @Query("select product from Product product join fetch product.shop where product.active = true "
            + "and product.shop.status = :status order by product.sold desc, product.id desc")
    List<Product> findFeatured(ShopStatus status, Pageable pageable);

    @Query("select product from Product product join fetch product.shop where product.active = true "
            + "and product.shop.status = :status and (product.category.id = :categoryId "
            + "or product.category.parent.id = :categoryId) order by product.id asc")
    List<Product> findPublicByCategory(Long categoryId, ShopStatus status);

    @Query("select product from Product product join fetch product.shop where product.id = :id "
            + "and product.active = true and product.shop.status = :status")
    Optional<Product> findPublicById(Long id, ShopStatus status);
}
