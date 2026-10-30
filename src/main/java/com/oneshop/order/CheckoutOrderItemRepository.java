package com.oneshop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import com.oneshop.entity.OrderItem;

public interface CheckoutOrderItemRepository extends JpaRepository<OrderItem, Long> {
}