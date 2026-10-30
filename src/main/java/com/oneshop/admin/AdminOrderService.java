package com.oneshop.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.common.Role;
import com.oneshop.entity.Order;
import com.oneshop.entity.User;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Admin/Manager xem đơn của mọi shop.
 * Chỉ Manager được gọi assignShipper (đã khóa bằng @PreAuthorize ở Controller).
 */
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final int PAGE_SIZE = 10;

    /** Các trạng thái coi là "đang giữ" của shipper. */
    private static final List<OrderStatus> HOLDING = List.of(
            OrderStatus.READY_TO_SHIP, OrderStatus.PICKED_UP, OrderStatus.SHIPPING);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * @param status      lọc theo trạng thái (null = tất cả)
     * @param unassigned  true = chỉ lấy đơn READY_TO_SHIP chưa có shipper
     */
    @Transactional(readOnly = true)
    public Page<Order> search(OrderStatus status, boolean unassigned, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        if (unassigned) {
            return orderRepository.findByStatusAndShipperIsNull(OrderStatus.READY_TO_SHIP, pageable);
        }
        return status == null ? orderRepository.findAllBy(pageable)
                              : orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<User> shippers() {
        return userRepository.findByRoleAndEnabledTrueAndLockedFalseOrderByEmailAsc(Role.SHIPPER);
    }

    /** Map shipperId → số đơn đang giữ (READY_TO_SHIP + PICKED_UP + SHIPPING). */
    @Transactional(readOnly = true)
    public Map<Long, Long> shipperWorkload() {
        return shippers().stream().collect(Collectors.toMap(
                User::getId,
                s -> orderRepository.countByShipperIdAndStatusIn(s.getId(), HOLDING)
        ));
    }

    /**
     * Chỉ phân công khi đơn đã READY_TO_SHIP (Vendor đã báo sẵn sàng giao).
     * Cho phép đổi shipper nếu vẫn còn READY_TO_SHIP.
     */
    @Transactional
    public void assignShipper(Long orderId, Long shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));

        if (order.getStatus() != OrderStatus.READY_TO_SHIP) {
            throw new BusinessException("Chỉ phân công shipper cho đơn đã sẵn sàng giao.");
        }

        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy shipper."));

        if (shipper.getRole() != Role.SHIPPER || !shipper.isEnabled() || shipper.isLocked()) {
            throw new BusinessException("Tài khoản này không phải shipper đang hoạt động.");
        }

        order.setShipper(shipper);
    }
}