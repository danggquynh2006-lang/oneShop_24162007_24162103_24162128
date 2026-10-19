package com.oneshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);
    Page<Order> findByShopId(Long shopId, Pageable pageable);
    Optional<Order> findByIdAndShopId(Long id, Long shopId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    List<Order> findByShopIdAndStatusAndDeliveredAtBetween(Long shopId, OrderStatus status,
                                                            LocalDateTime from, LocalDateTime to);
}
