package com.oneshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oneshop.common.OrderStatus;
import com.oneshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    /** Các dòng đơn hàng của user cho sản phẩm này với đơn ở trạng thái status (dùng để kiểm tra "đã mua"). */
    @Query("select oi from OrderItem oi where oi.order.user.id = :userId and oi.product.id = :productId "
            + "and oi.order.status = :status order by oi.id")
    List<OrderItem> findPurchased(@Param("userId") Long userId, @Param("productId") Long productId,
                                  @Param("status") OrderStatus status);
}