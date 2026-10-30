package com.oneshop.order;

import java.util.List;
import com.oneshop.common.OrderStatus;

public enum OrderTab {
    NEW("Mới", List.of(OrderStatus.NEW)),
    CONFIRMED("Đã xác nhận", List.of(OrderStatus.CONFIRMED, OrderStatus.READY_TO_SHIP)),
    SHIPPING("Đang giao", List.of(OrderStatus.PICKED_UP, OrderStatus.SHIPPING)),
    DELIVERED("Đã giao", List.of(OrderStatus.DELIVERED)),
    CANCELLED("Đã hủy", List.of(OrderStatus.CANCELLED)),
    RETURN("Trả hàng / Hoàn tiền",
           List.of(OrderStatus.RETURN_REQUESTED, OrderStatus.RETURNED, OrderStatus.REFUNDED));

    private final String label;
    private final List<OrderStatus> statuses;

    OrderTab(String label, List<OrderStatus> statuses) {
        this.label = label;
        this.statuses = statuses;
    }

    public String getLabel() { return label; }
    public List<OrderStatus> getStatuses() { return statuses; }
}