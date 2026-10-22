package com.oneshop.order;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.BusinessException;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    public record Row(Long id, String shopName, OrderStatus status,
                      java.math.BigDecimal total, java.time.LocalDateTime createdAt) {}
    
    public record ItemView(Long productId, String name, int quantity,
            java.math.BigDecimal unitPrice, java.math.BigDecimal lineTotal) {}

    private final UserOrderRepository orderRepo;
    private final UserOrderItemRepository itemRepo;
    private final CheckoutProductRepository productRepo;

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
                        i.getUnitPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity()))))
                .toList();
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
    }

    @Transactional
    public void requestReturn(User user, Long id) {
        Order order = detail(user, id);
        if (!OrderRules.userCanRequestReturn(order.getStatus())) {
            throw new BusinessException("Chỉ có thể yêu cầu trả hàng khi đơn đã giao.");
        }
        int changed = orderRepo.changeStatus(id, user.getId(),
                OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED);
        if (changed == 0) {
            throw new BusinessException("Đơn hàng vừa được cập nhật, vui lòng tải lại trang.");
        }
    }
}