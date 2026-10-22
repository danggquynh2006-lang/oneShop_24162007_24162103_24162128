package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
}