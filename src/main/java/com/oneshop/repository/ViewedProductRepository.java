package com.oneshop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.ViewedProduct;

public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Long> {

    Optional<ViewedProduct> findByUserIdAndProductId(Long userId, Long productId);

    @Query("select v from ViewedProduct v join fetch v.product p "
         + "where v.user.id = :userId order by v.viewedAt desc")
    List<ViewedProduct> findRecent(@Param("userId") Long userId, Pageable pageable);
}