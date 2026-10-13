package com.oneshop.common;

public enum OrderStatus {
    NEW("Đơn mới"),
    CONFIRMED("Đã xác nhận"),
    PICKED_UP("Đã lấy hàng"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy"),
    RETURN_REQUESTED("Yêu cầu trả hàng"),
    RETURNED("Đã trả hàng"),
    REFUNDED("Đã hoàn tiền");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}