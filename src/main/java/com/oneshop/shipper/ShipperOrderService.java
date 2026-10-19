package com.oneshop.shipper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentMethod;
import com.oneshop.common.PaymentStatus;
import com.oneshop.common.ResourceNotFoundException;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.Payment;
import com.oneshop.order.OrderRules;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;
import com.oneshop.vendor.VendorOrderService;

import lombok.RequiredArgsConstructor;

/**
 * Shipper chỉ thao tác trên đơn ĐƯỢC PHÂN CÔNG cho mình (kiểm tra ở server bằng findByIdAndShipperId).
 * Luồng: CONFIRMED -> PICKED_UP -> SHIPPING -> DELIVERED (hoặc giao thất bại -> CANCELLED).
 */
@Service
@RequiredArgsConstructor
public class ShipperOrderService {

    private static final int PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentRepository paymentRepository;
    private final VendorOrderService vendorOrderService;

    // ===== Xem =====

    @Transactional(readOnly = true)
    public Page<Order> list(Long shipperId, OrderStatus status, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        return status == null
                ? orderRepository.findByShipperId(shipperId, pageable)
                : orderRepository.findByShipperIdAndStatus(shipperId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Order get(Long shipperId, Long orderId) {
        return orderRepository.findByIdAndShipperId(orderId, shipperId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng được phân công cho bạn."));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> items(Long orderId) {
        return itemRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public Payment payment(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElse(null);
    }

    // ===== Cập nhật trạng thái =====

    /** CONFIRMED -> PICKED_UP */
    @Transactional
    public void pickUp(Long shipperId, Long orderId) {
        move(get(shipperId, orderId), OrderStatus.PICKED_UP);
    }

    /** PICKED_UP -> SHIPPING */
    @Transactional
    public void startShipping(Long shipperId, Long orderId) {
        move(get(shipperId, orderId), OrderStatus.SHIPPING);
    }

    /** SHIPPING -> DELIVERED. Dùng lại logic của Vendor: tăng sold, chốt chiết khấu, COD thành PAID. */
    @Transactional
    public void delivered(Long shipperId, Long orderId) {
        vendorOrderService.markDelivered(get(shipperId, orderId));
    }

    /** Giao thất bại: SHIPPING -> CANCELLED, trả lại tồn kho, đơn đã thanh toán online thì chờ hoàn tiền. */
    @Transactional
    public void failed(Long shipperId, Long orderId) {
        Order order = get(shipperId, orderId);
        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new BusinessException("Chỉ báo giao thất bại khi đơn đang giao.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        for (OrderItem item : itemRepository.findByOrderId(orderId)) {
            item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
        }
        paymentRepository.findByOrderId(orderId)
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .ifPresent(payment -> payment.setStatus(PaymentStatus.REFUND_PENDING));
    }

    private void move(Order order, OrderStatus next) {
        if (!OrderRules.canTransition(order.getStatus(), next)) {
            throw new BusinessException("Trạng thái đơn hàng không hợp lệ.");
        }
        order.setStatus(next);
    }

    // ===== Thống kê =====

    @Transactional(readOnly = true)
    public ShipperStatistics statistics(Long shipperId, int year, int month) {
        int y = Math.min(Math.max(year, 2000), 2100);
        int m = Math.min(Math.max(month, 1), 12);

        LocalDateTime from = LocalDate.of(y, 1, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1).minusNanos(1);
        List<Order> deliveredInYear = orderRepository
                .findByShipperIdAndStatusAndDeliveredAtBetween(shipperId, OrderStatus.DELIVERED, from, to);

        List<ShipperStatistics.Row> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            final int month1 = i;
            List<Order> orders = deliveredInYear.stream()
                    .filter(o -> o.getDeliveredAt().getMonthValue() == month1).toList();
            months.add(new ShipperStatistics.Row("Tháng " + i, orders.size(), cod(orders)));
        }

        int daysInMonth = LocalDate.of(y, m, 1).lengthOfMonth();
        List<Order> inMonth = deliveredInYear.stream()
                .filter(o -> o.getDeliveredAt().getMonthValue() == m).toList();
        List<ShipperStatistics.Row> days = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            final int day = d;
            List<Order> orders = inMonth.stream()
                    .filter(o -> o.getDeliveredAt().getDayOfMonth() == day).toList();
            days.add(new ShipperStatistics.Row(String.format("%02d/%02d/%d", d, m, y), orders.size(), cod(orders)));
        }

        long inProgress = orderRepository.countByShipperIdAndStatus(shipperId, OrderStatus.PICKED_UP)
                + orderRepository.countByShipperIdAndStatus(shipperId, OrderStatus.SHIPPING);

        return new ShipperStatistics(y, m,
                inMonth.size(), cod(inMonth),
                deliveredInYear.size(), cod(deliveredInYear),
                orderRepository.countByShipperIdAndStatus(shipperId, OrderStatus.CONFIRMED),
                inProgress,
                orderRepository.countByShipperIdAndStatus(shipperId, OrderStatus.CANCELLED),
                days, months);
    }

    /** Tổng tiền thu hộ: chỉ cộng các đơn COD (đơn đã trả online thì shipper không thu tiền). */
    private BigDecimal cod(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getPaymentMethod() == PaymentMethod.COD)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}