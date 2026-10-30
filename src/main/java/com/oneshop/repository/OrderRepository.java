package com.oneshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // ===== Giữ nguyên của Jung =====
    Page<Order> findByShopIdAndStatus(Long shopId, OrderStatus status, Pageable pageable);
    Page<Order> findByShopId(Long shopId, Pageable pageable);
    Optional<Order> findByIdAndShopId(Long id, Long shopId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    List<Order> findByShopIdAndStatusAndDeliveredAtBetween(Long shopId, OrderStatus status,
                                                            LocalDateTime from, LocalDateTime to);

    // ===== Phần thêm cho Admin / Shipper (TV1) =====

    /** Admin/Manager: danh sách mọi đơn. */
    @EntityGraph(attributePaths = {"user", "shop", "shipper"})
    Page<Order> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "shop", "shipper"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /** Đơn sẵn sàng giao nhưng chưa phân công shipper. */
    @EntityGraph(attributePaths = {"user", "shop", "shipper"})
    Page<Order> findByStatusAndShipperIsNull(OrderStatus status, Pageable pageable);

    /** Shipper: chỉ đơn được phân công cho mình. */
    @EntityGraph(attributePaths = {"user", "shop"})
    Page<Order> findByShipperId(Long shipperId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "shop"})
    Page<Order> findByShipperIdAndStatus(Long shipperId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "shop"})
    Optional<Order> findByIdAndShipperId(Long id, Long shipperId);

    long countByShipperIdAndStatus(Long shipperId, OrderStatus status);

    /** Số đơn đang giữ của shipper (chưa giao xong). */
    long countByShipperIdAndStatusIn(Long shipperId, List<OrderStatus> statuses);

    List<Order> findByShipperIdAndStatusAndDeliveredAtBetween(Long shipperId, OrderStatus status,
                                                               LocalDateTime from, LocalDateTime to);
}