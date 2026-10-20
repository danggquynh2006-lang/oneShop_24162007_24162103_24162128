package com.oneshop.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import com.oneshop.entity.Promotion;
import jakarta.persistence.LockModeType;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findByShopId(Long shopId, Pageable pageable);
    Optional<Promotion> findByIdAndShopId(Long id, Long shopId);
    boolean existsByCodeIgnoreCase(String code);
    Optional<Promotion> findByCodeIgnoreCase(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Promotion> findWithLockByCodeIgnoreCase(String code);
}
