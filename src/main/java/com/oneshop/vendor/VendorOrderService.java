package com.oneshop.vendor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class VendorOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<Order> list(Long shopId, OrderStatus status, int page) {
        var pageable = PageRequest.of(Math.max(0, page), 10, Sort.by("createdAt").descending());
        return status == null ? orderRepository.findByShopId(shopId, pageable)
                : orderRepository.findByShopIdAndStatus(shopId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Order get(Long shopId, Long orderId) {
        return orderRepository.findByIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng của shop."));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> items(Long orderId) { return itemRepository.findByOrderId(orderId); }

    @Transactional(readOnly = true)
    public Payment payment(Long orderId) { return paymentRepository.findByOrderId(orderId).orElse(null); }

    @Transactional
    public void confirm(Long shopId, Long orderId) {
        Order order = get(shopId, orderId);
        requireStatus(order, OrderStatus.NEW);
        order.setStatus(OrderStatus.CONFIRMED);
    }

    @Transactional
    public void reject(Long shopId, Long orderId) {
        Order order = get(shopId, orderId);
        requireStatus(order, OrderStatus.NEW);
        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(orderId, false);
        paymentRepository.findByOrderId(orderId).filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .ifPresent(payment -> payment.setStatus(PaymentStatus.REFUND_PENDING));
    }

    @Transactional
    public void resolveReturn(Long shopId, Long orderId, boolean accept) {
        Order order = get(shopId, orderId);
        requireStatus(order, OrderStatus.RETURN_REQUESTED);
        if (!accept) {
            order.setStatus(OrderStatus.DELIVERED);
            return;
        }
        order.setStatus(OrderStatus.RETURNED);
        restoreStock(orderId, true);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.REFUND_PENDING);
            payment.setRefundRequestedAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void completeCashRefund(Long shopId, Long orderId) {
        Order order = get(shopId, orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thanh toán."));
        if (order.getStatus() != OrderStatus.RETURNED || payment.getMethod() != PaymentMethod.COD
                || payment.getStatus() != PaymentStatus.REFUND_PENDING)
            throw new BusinessException("Đơn chưa đủ điều kiện xác nhận hoàn tiền COD.");
        payment.setStatus(PaymentStatus.REFUNDED);
        order.setStatus(OrderStatus.REFUNDED);
    }

    @Transactional
    public void markDelivered(Order order) {
        requireStatus(order, OrderStatus.SHIPPING);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setCommissionRateSnapshot(order.getShop().getCommissionRate());
        for (OrderItem item : itemRepository.findByOrderId(order.getId())) {
            item.getProduct().setSold(item.getProduct().getSold() + item.getQuantity());
        }
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
            });
        }
    }

    private void restoreStock(Long orderId, boolean returned) {
        for (OrderItem item : itemRepository.findByOrderId(orderId)) {
            item.getProduct().setStock(item.getProduct().getStock() + item.getQuantity());
            if (returned) item.getProduct().setSold(Math.max(0, item.getProduct().getSold() - item.getQuantity()));
        }
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) throw new BusinessException("Trạng thái đơn hàng không hợp lệ.");
    }
}
