package com.oneshop.order;

import static com.oneshop.common.OrderStatus.*;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import com.oneshop.common.OrderStatus;

public final class OrderRules {

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = new EnumMap<>(OrderStatus.class);

    static {
        NEXT.put(NEW, EnumSet.of(CONFIRMED, CANCELLED));
        NEXT.put(CONFIRMED, EnumSet.of(READY_TO_SHIP, CANCELLED));
        NEXT.put(READY_TO_SHIP, EnumSet.of(PICKED_UP));
        NEXT.put(PICKED_UP, EnumSet.of(SHIPPING));
        NEXT.put(SHIPPING, EnumSet.of(DELIVERED));
        NEXT.put(DELIVERED, EnumSet.of(RETURN_REQUESTED));
        NEXT.put(RETURN_REQUESTED, EnumSet.of(RETURNED, DELIVERED));
        NEXT.put(RETURNED, EnumSet.of(REFUNDED));
        NEXT.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
        NEXT.put(REFUNDED, EnumSet.noneOf(OrderStatus.class));
    }

    private OrderRules() {}

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        return NEXT.getOrDefault(from, Set.of()).contains(to);
    }

    public static void assertTransition(OrderStatus from, OrderStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Không thể chuyển đơn từ " + from + " sang " + to);
        }
    }

    public static boolean userCanCancel(OrderStatus status) {
        return status == NEW;
    }

    public static boolean userCanRequestReturn(OrderStatus status) {
        return status == DELIVERED;
    }
}