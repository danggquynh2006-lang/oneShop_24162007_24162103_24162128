package com.oneshop.vendor;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.oneshop.common.OrderStatus;
import com.oneshop.entity.Order;
import com.oneshop.entity.Shop;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.ShopRepository;

@SpringBootTest @ActiveProfiles("demo") @Transactional
class VendorRevenueTests {
    @Autowired private VendorRevenueService revenueService;
    @Autowired private ShopRepository shopRepository;
    @Autowired private OrderRepository orderRepository;

    private Shop demoShop() {
        return shopRepository.findAll().stream()
                .filter(shop -> shop.getName().equals("Lumière Beauty")).findFirst().orElseThrow();
    }

    private Order deliveredOrder() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED).findFirst().orElseThrow();
    }

    @Test
    void returnedOrderIsNotCountedInRevenue() {
        Long shopId = demoShop().getId();
        Order delivered = deliveredOrder();
        LocalDate day = delivered.getDeliveredAt().toLocalDate();
        assertEquals(1, revenueService.daily(shopId, day).orders());

        delivered.setStatus(OrderStatus.RETURNED);

        var summary = revenueService.daily(shopId, day);
        assertEquals(0, summary.orders());
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.gross()));
    }
    @Test
    void commissionUsesRateSavedAtDeliveryTime() {
        Shop shop = demoShop();
        LocalDate day = deliveredOrder().getDeliveredAt().toLocalDate();

        shop.setCommissionRate(new BigDecimal("20.00"));

        var summary = revenueService.daily(shop.getId(), day);
        assertEquals(0, new BigDecimal("27600").compareTo(summary.commission()));
        assertEquals(0, new BigDecimal("317400").compareTo(summary.net()));
    }
}