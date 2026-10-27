package com.oneshop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    long countByProductId(Long productId);

    @Query("select f from Favorite f join fetch f.product p "
         + "where f.user.id = :userId order by f.createdAt desc")
    List<Favorite> findByUser(@Param("userId") Long userId);
    
    @Query("select distinct f.product.category.id from Favorite f where f.user.id = :userId")
    List<Long> categoryIds(@Param("userId") Long userId);

    @Query("select f.product.id from Favorite f where f.user.id = :userId")
    List<Long> productIds(@Param("userId") Long userId);
}