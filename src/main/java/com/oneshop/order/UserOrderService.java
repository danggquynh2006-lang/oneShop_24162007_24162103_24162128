package com.oneshop.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.common.PaymentStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.Payment;
import com.oneshop.entity.ReturnRequest;
import com.oneshop.entity.User;
import com.oneshop.repository.PaymentRepository;
import com.oneshop.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    public static final String OTHER = "Khác";
    public static final List<String> REASONS = List.of(
            "Sản phẩm bị lỗi hoặc hư hỏng",
            "Sản phẩm không đúng mô tả",
            "Giao thiếu hàng hoặc sai hàng",
            OTHER);

    public record Row(Long id, String shopName, OrderStatus status,
                      BigDecimal total, LocalDateTime createdAt) {}

    public record ItemView(Long productId, String name, int quantity,
                           BigDecimal unitPrice, BigDecimal lineTotal) {}

    public record ReturnInfo(String reason, String note, LocalDateTime createdAt) {}

    private final UserOrderRepository orderRepo;
    private final UserOrderItemRepository itemRepo;
    private final CheckoutProductRepository productRepo;
    private final PaymentRepository paymentRepo;
    private final ReturnRequestRepository returnRepo;

    @Transactional(readOnly = true)
    public List<Row> list(User user, OrderTab tab) {
        return orderRepo.findByTab(user.getId(), tab.getStatuses()).stream()
                .map(o -> new Row(o.getId(), o.getShop().getName(), o.getStatus(),
                        o.getTotal(), o.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Order detail(User user, Long id) {
        return orderRepo.findDetail(id, user.getId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng."));
    }

    @Transactional(readOnly = true)
    public List<ItemView> items(Long orderId) {
        return itemRepo.findByOrder(orderId).stream()
                .map(i -> new ItemView(
                        i.getProduct().getId(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentStatus> paymentStatus(Long orderId) {
        return paymentRepo.findByOrderId(orderId).map(Payment::getStatus);
    }

    @Transactional(readOnly = true)
    public Optional<ReturnInfo> returnInfo(Long orderId) {
        return returnRepo.findByOrderId(orderId)
                .map(r -> new ReturnInfo(r.getReason(), r.getNote(), r.getCreatedAt()));
    }

    @Transactional
    public void cancel(User user, Long id) {
        Order order = detail(user, id);
        if (!OrderRules.userCanCancel(order.getStatus())) {
            throw new BusinessException("Chỉ có thể hủy đơn khi shop chưa xác nhận.");
        }
        int changed = orderRepo.changeStatus(id, user.getId(), OrderStatus.NEW, OrderStatus.CANCELLED);
        if (changed == 0) {
            throw new BusinessException("Đơn hàng vừa được cập nhật, vui lòng tải lại trang.");
        }
        for (OrderItem item : itemRepo.findByOrder(id)) {
            productRepo.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
        paymentRepo.findByOrderId(id).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.PAID) {
                p.setStatus(PaymentStatus.REFUND_PENDING);
                p.setRefundRequestedAt(LocalDateTime.now());
            }
        });
    }

    @Transactional
    public void requestReturn(User user, Long id, String reason, String note) {
        String r = reason == null ? "" : reason.trim();
        String n = note == null ? "" : note.trim();

        if (!REASONS.contains(r)) {
            throw new BusinessException("Vui lòng chọn lý do trả hàng.");
        }
        if (n.length() > 500) {
            throw new BusinessException("Mô tả chi tiết tối đa 500 ký tự.");
        }
        if (OTHER.equals(r) && n.length() < 10) {
            throw new BusinessException("Với lý do \"Khác\", vui lòng mô tả ít nhất 10 ký tự.");
        }

        Order order = detail(user, id);
        if (!OrderRules.userCanRequestReturn(order.getStatus())) {
            throw new BusinessException("Chỉ có thể yêu cầu trả hàng khi đơn đã giao.");
        }
        int changed = orderRepo.changeStatus(id, user.getId(),
                OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED);
        if (changed == 0) {
            throw new BusinessException("Đơn hàng vừa được cập nhật, vui lòng tải lại trang.");
        }
        returnRepo.save(ReturnRequest.builder()
                .order(order)
                .reason(r)
                .note(n.isEmpty() ? null : n)
                .build());
    }
}