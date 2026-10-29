package com.oneshop.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.oneshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    boolean existsByProductId(Long productId);

    @Query("select count(item) > 0 from OrderItem item where item.product.id = :productId "
            + "and item.order.user.id = :userId and item.order.status = com.oneshop.common.OrderStatus.DELIVERED")
    boolean hasDeliveredPurchase(Long productId, Long userId);
}
