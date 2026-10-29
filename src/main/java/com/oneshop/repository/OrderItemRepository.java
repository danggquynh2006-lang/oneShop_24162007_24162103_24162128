package com.oneshop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    boolean existsByProductId(Long productId);
}
