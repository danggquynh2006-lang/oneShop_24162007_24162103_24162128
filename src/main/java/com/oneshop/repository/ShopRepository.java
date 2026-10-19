package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.common.ShopStatus;
import com.oneshop.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long ownerId);
    Optional<Shop> findByIdAndStatus(Long id, ShopStatus status);
    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);
}
