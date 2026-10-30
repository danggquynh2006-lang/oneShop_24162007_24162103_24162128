package com.oneshop.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.oneshop.entity.OrderItem;

public interface UserOrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select i from OrderItem i where i.order.id = :orderId order by i.id")
    List<OrderItem> findByOrder(@Param("orderId") Long orderId);
}