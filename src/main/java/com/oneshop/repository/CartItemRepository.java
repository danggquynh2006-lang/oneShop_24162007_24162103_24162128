package com.oneshop.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @Query("select i from CartItem i join fetch i.product p join fetch p.shop s "
         + "where i.cart.user.id = :userId order by s.id, i.id")
    List<CartItem> findDetailByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(i.quantity), 0) from CartItem i where i.cart.user.id = :userId")
    long totalQuantity(@Param("userId") Long userId);
}