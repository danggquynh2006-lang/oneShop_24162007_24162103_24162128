package com.oneshop.admin;

import java.util.List;

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

/** Admin/Manager xem đơn của mọi shop và phân công shipper cho đơn đã xác nhận. */
@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<Order> search(OrderStatus status, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return status == null ? orderRepository.findAllBy(pageable) : orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<User> shippers() {
        return userRepository.findByRoleAndEnabledTrueAndLockedFalseOrderByEmailAsc(Role.SHIPPER);
    }

    /** Chỉ phân công (hoặc đổi shipper) khi đơn đã được shop xác nhận và shipper chưa lấy hàng. */
    @Transactional
    public void assignShipper(Long orderId, Long shipperId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Chỉ phân công shipper cho đơn đã xác nhận và chưa lấy hàng.");
        }
        User shipper = userRepository.findById(shipperId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy shipper."));
        if (shipper.getRole() != Role.SHIPPER || !shipper.isEnabled() || shipper.isLocked()) {
            throw new BusinessException("Tài khoản này không phải shipper đang hoạt động.");
        }
        order.setShipper(shipper);
    }
}